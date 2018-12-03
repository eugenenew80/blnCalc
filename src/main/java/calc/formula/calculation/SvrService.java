package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.svr.*;
import calc.formula.*;
import calc.formula.exception.CalcServiceException;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.formula.service.ParamService;
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
import static java.util.Optional.*;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class SvrService {
    private static final Logger logger = LoggerFactory.getLogger(SvrService.class);
    private final CalcService calcService;
    private final ParamService paramService;
    private final MessageService messageService;
    private final SvrHeaderRepo svrHeaderRepo;
    private final SvrLineRepo svrLineRepo;
    private final MeteringPointSettingRepo meteringPointSettingRepo;
    private final SvrNoteRepo svrNoteRepo;
    private final MeteringPointSettingNoteRepo mpsRepo;
    private static final String docCode = "SVR";

    public boolean calc(Long headerId) {
        logger.info("Service value reconciliation for header " + headerId + " started");
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
            deleteLines(header);
            calcLines(header, context);
            copyNotes(header);

            List<SvrResultLine> resultLines = svrLineRepo.findAllByHeaderId(headerId);
            DataTypeEnum dataType = getDocDataType(resultLines);

            header.setDataType(dataType);
            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("Service value reconciliation for header " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Service value reconciliation for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calcLines(SvrResultHeader header, CalcContext context) {
        List<MeteringPointSetting> lines = meteringPointSettingRepo.findAllByContractIdAndDate(
            header.getContract().getId(),
            header.getStartDate(),
            header.getEndDate()
        );

        List<SvrResultLine> resultLines = new ArrayList<>();
        for (MeteringPointSetting line : lines) {
            if (line.getOrganization()!=null && !line.getOrganization().equals(header.getOrganization()))
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null)
                continue;

            Parameter param = ofNullable(line.getParam()).orElse(paramService.getValues().get("AB"));
            if (param == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(meteringPoint, param);

            CalcProperty property = CalcProperty.builder()
                .processOrder(ProcessOrderEnum.READ_CALC)
                .build();

            Double val = null;
            DataTypeEnum dataType = null;
            try {
                CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
                val = result != null ? result.getDoubleValue() : null;
                val = round(val, param);
                dataType = getRowDataType(meteringPoint, context);
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                e.printStackTrace();
            }

            if (val != null)
                val = Math.abs(val);

            SvrResultLine resultLine = new SvrResultLine();
            resultLine.setHeader(header);
            resultLine.setMeteringPoint(meteringPoint);
            resultLine.setParam(param);
            resultLine.setTypeCode(line.getTypeCode());
            resultLine.setVal(val);
            resultLine.setDataType(dataType);
            resultLine.setOrganization(header.getOrganization());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines(List<SvrResultLine> lines) {
        svrLineRepo.save(lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(SvrResultHeader header) {
        List<SvrResultLine> lines = svrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            svrLineRepo.delete(lines.get(i));
        svrLineRepo.flush();

        List<SvrResultNote> notes = svrNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            svrNoteRepo.delete(notes.get(i));
        svrNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(SvrResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        svrHeaderRepo.save(header);
    }
}
