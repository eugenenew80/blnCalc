package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.svr.*;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class SvrService {
    private static final Logger logger = LoggerFactory.getLogger(SvrService.class);
    private final SvrHeaderRepo svrHeaderRepo;
    private final SvrLineRepo svrLineRepo;
    private final MeteringPointSettingRepo meteringPointSettingRepo;
    private final PeriodTimeValueService periodTimeValueService;
    private final SvrNoteRepo svrNoteRepo;
    private final MeteringPointSettingNoteRepo mpsRepo;
    private static final String docCode = "SVR";

    public boolean calc(SvrHeader header) {
        try {
            logger.info("Service value reconcilation for header " + header.getId() + " started");
            header = svrHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return false;

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .contextType(ContextType.DEFAULT)
                .values(new HashMap<>())
                .build();

            List<MeteringPointSetting> lines = meteringPointSettingRepo.findAllByContractIdAndDate(
                header.getContract().getId(),
                header.getStartDate(),
                header.getEndDate()
            );

            List<SvrLine> resultLines = new ArrayList<>();
            for (MeteringPointSetting line : lines) {
                if (line.getOrganization()!=null && !line.getOrganization().equals(header.getOrganization()))
                    continue;

                MeteringPoint meteringPoint = line.getMeteringPoint();
                if (meteringPoint == null) continue;

                PeriodTimeValueExpression ap = PeriodTimeValueExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode("A+")
                    .periodType(context.getPeriodType())
                    .rate(1d)
                    .startHour((byte) 0)
                    .endHour((byte) 23)
                    .service(periodTimeValueService)
                    .context(context)
                    .build();

                PeriodTimeValueExpression am = PeriodTimeValueExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode("A-")
                    .periodType(context.getPeriodType())
                    .rate(1d)
                    .startHour((byte) 0)
                    .endHour((byte) 23)
                    .service(periodTimeValueService)
                    .context(context)
                    .build();

                Double val = Math.abs(Optional.of(ap.doubleValue()).orElse(0d) - Optional.of(am.doubleValue()).orElse(0d));

                SvrLine resultLine = new SvrLine();
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(line.getMeteringPoint());
                resultLine.setTypeCode(line.getTypeCode());
                resultLine.setVal(val);
                resultLine.setOrganization(header.getOrganization());

                if (resultLine.getTranslates() == null)
                    resultLine.setTranslates(new ArrayList<>());

                for (MeteringPointSettingTranslate lineTranslate : line.getTranslates()) {
                    SvrLineTranslate resultLineTranslate = new SvrLineTranslate();
                    resultLineTranslate.setLang(lineTranslate.getLang());
                    resultLineTranslate.setLine(resultLine);
                    resultLineTranslate.setName(lineTranslate.getName());
                    resultLine.getTranslates().add(resultLineTranslate);
                }

                resultLines.add(resultLine);
                saveLines(resultLines);
            }

            copyNotes(header);
            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            header.setDataType(DataTypeEnum.OPER);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("Metering reading for header " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyNotes(SvrHeader header) {
        List<MeteringPointSettingNote> mpsNotes = mpsRepo.findAllByContractId(header.getContract().getId());

        List<SvrNote> resultNotes = new ArrayList<>();
        for (MeteringPointSettingNote note : mpsNotes) {
            SvrNote resultNote = new SvrNote();
            resultNote.setHeader(header);
            resultNote.setNoteNum(note.getNoteNum());
            resultNote.setOrg(note.getOrg());

            resultNote.setTranslates(Optional.ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (MeteringPointSettingNoteTranslate noteTranslate : note.getTranslates()) {
                SvrNoteTranslate resultNoteTranslate = new SvrNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        svrNoteRepo.save(resultNotes);
        svrNoteRepo.flush();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<SvrLine> lines) {
        svrLineRepo.save(lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(SvrHeader header) {
        List<SvrLine> lines = svrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            svrLineRepo.delete(lines.get(i));
        svrLineRepo.flush();

        List<SvrNote> notes = svrNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            svrNoteRepo.delete(notes.get(i));
        svrNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateStatus(SvrHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        svrHeaderRepo.save(header);
    }
}
