package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.svr.*;
import calc.formula.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import static calc.util.Util.*;
import static java.lang.Math.abs;
import static java.util.Optional.*;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class SvrService {
    private static final Logger logger = LoggerFactory.getLogger(SvrService.class);
    private static final String docCode = "SVR";
    private final CalcService calcService;
    private final ParamService paramService;
    private final MessageService messageService;
    private final SvrHeaderRepo svrHeaderRepo;
    private final SvrLineRepo svrLineRepo;
    private final SvrPartRepo svrPartRepo;
    private final MeteringPointSettingRepo meteringPointSettingRepo;
    private final SvrNoteRepo svrNoteRepo;
    private final MeteringPointSettingNoteRepo mpsRepo;

    public boolean calc(Long headerId) {
        logger.info(docCode + " for header " + headerId + " started");
        SvrResultHeader header = svrHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return false;

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .header(header)
            .traceEnabled(true)
            .formulaBehaviour(FormulaBehaviourEnum.EXACTLY_ONE)
            .defContextType(ContextTypeEnum.DEFAULT)
            .build();

        try {
            header.setDataType(null);
            updateStatus(header, BatchStatusEnum.P);
            messageService.deleteMessages(header);

            DataTypeEnum dataType;
            Double total;
            if (header.getIsAggregation()) {
                List<SvrResultPart> parts = svrPartRepo.findAllByHeaderId(headerId);
                dataType = getDocDataType(parts);

                total = parts.stream()
                    .filter(t -> t.getPart() != null)
                    .map(t -> t.getPart())
                    .filter(t -> t.getIsPartOfAggregation())
                    .filter(t -> t.getIsCurrentRecord())
                    .filter(t -> t.getVal() != null)
                    .map(t -> t.getVal())
                    .reduce((t1, t2) -> ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d))
                    .orElse(0d);
            }
            else {
                deleteLines(header);
                calcLines(header, context);
                copyNotes(header);

                List<SvrResultLine> resultLines = svrLineRepo.findAllByHeaderId(headerId);
                dataType = getDocDataType(resultLines);

                total = resultLines.stream()
                    .filter(t -> t.getOrganization() != null && t.getOrganization().equals(header.getOrganization()))
                    .filter(t -> !t.getIsTotal())
                    .filter(t -> t.getVal() != null)
                    .map(t -> t.getVal())
                    .reduce((t1, t2) -> ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d))
                    .orElse(0d);
            }

            header.setVal(total);
            header.setDataType(dataType);
            header.setLastUpdateDate(LocalDateTime.now());
            updateStatus(header, BatchStatusEnum.C);

            logger.info(docCode + " for header " + header.getId() + " completed");
            return true;
        }
        catch (Exception e) {
            logger.error(docCode + " for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();

            updateStatus(header, BatchStatusEnum.E);
            return false;
        }
    }

    private void calcLines(SvrResultHeader header, CalcContext context) {
        List<MeteringPointSetting> lines = meteringPointSettingRepo.findAllByContractIdAndDate(
            header.getContract().getId(),
            header.getStartDate(),
            header.getEndDate()
        );

        List<SvrResultLine> results = new ArrayList<>();
        for (MeteringPointSetting line : lines) {
            if (line.getOrganization() != null && !line.getOrganization().equals(header.getOrganization()))
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = ofNullable(line.getParam()).orElse(paramService.getParam("AB"));

            SvrResultLine result = new SvrResultLine();
            result.setHeader(header);
            result.setMeteringPoint(meteringPoint);
            result.setParam(param);
            result.setTypeCode(line.getTypeCode());
            result.setOrganization(header.getOrganization());
            result.setIsTotal(line.getIsTotal());
            copyTranslates(line, result);
            results.add(result);

            if (meteringPoint == null || param == null)
                continue;

            CalcProperty property = CalcProperty.builder()
                .processOrder(ProcessOrderEnum.READ_CALC)
                .determiningMethod(line.getMethod())
                .build();

            Double val = null;
            DataTypeEnum dataType = null;
            try {
                CalcResult calc = calcService.calcValue(meteringPoint, param, context, property);
                val = calc != null ? calc.getDoubleValue() : null;
                val = round(val, param);
                dataType = getRowDataType(context);
            }
            catch (CalcServiceException e) {
                messageService.addMessage(header, line, e);
                e.printStackTrace();
            }

            result.setVal(val);
            result.setDataType(dataType);
        }
        saveLines(results);
    }

    private void copyTranslates(MeteringPointSetting line, SvrResultLine resultLine) {
        if (resultLine.getTranslates() == null)
            resultLine.setTranslates(new ArrayList<>());

        for (MeteringPointSettingTranslate lineTranslate : line.getTranslates()) {
            SvrResultLineTranslate resultLineTranslate = new SvrResultLineTranslate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);
            resultLineTranslate.setName(lineTranslate.getName());
            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyNotes(SvrResultHeader header) {
        List<MeteringPointSettingNote> mpsNotes = mpsRepo.findAllByContractId(header.getContract().getId());

        List<SvrResultNote> resultNotes = new ArrayList<>();
        for (MeteringPointSettingNote note : mpsNotes) {
            SvrResultNote resultNote = new SvrResultNote();
            resultNote.setHeader(header);
            resultNote.setNoteNum(note.getNoteNum());
            resultNote.setOrg(note.getOrg());

            resultNote.setTranslates(ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (MeteringPointSettingNoteTranslate noteTranslate : note.getTranslates()) {
                SvrResultNoteTranslate resultNoteTranslate = new SvrResultNoteTranslate();
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines(List<SvrResultLine> lines) {
        svrLineRepo.save(lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void deleteLines(SvrResultHeader header) {
        List<SvrResultLine> lines = svrLineRepo.findAllByHeaderId(header.getId());
        svrLineRepo.delete(lines);
        svrLineRepo.flush();

        List<SvrResultNote> notes = svrNoteRepo.findAllByHeaderId(header.getId());
        svrNoteRepo.delete(notes);
        svrNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void updateStatus(SvrResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        svrHeaderRepo.save(header);
    }
}