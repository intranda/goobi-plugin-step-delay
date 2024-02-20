package de.intranda.goobi.plugins;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class MovingWallDelayPlugin implements IDelayPlugin, IStepPlugin {

    private static final long serialVersionUID = -6552069540244015278L;
    private static final String PLUGIN_NAME = "intranda_delay_moving_wall";
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private DateFormat yearFormat = new SimpleDateFormat("yyyy");
    private static final String MOVINGWALL_PROPERTYNAME = "Movingwall timestamp";
    private Step step;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
    }

    @Override
    public boolean execute() {
        // set step status to inwork
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step.setBearbeitungsbeginn(new Date());
        try {
            Date movingWallDate = getMovingWallDate(step);
            Helper.addMessageToProcessJournal(step.getProzess().getId(), LogType.DEBUG,
                    "Process is blocked until " + dateFormat.format(movingWallDate), "delay");

            StepManager.saveStep(step);
        } catch (ParseException | IllegalArgumentException | DAOException e) {
            log.error("Error while saving the step", e);
        }
        return false;
    }

    protected Date getMovingWallDate(Step step) throws ParseException, IllegalArgumentException {
        if (step == null) {
            throw new IllegalArgumentException("Must pass a step to determing moving wall date");
        }
        org.goobi.beans.Process prozess = step.getProzess();
        List<Processproperty> properties = prozess.getEigenschaften();
        for (Processproperty property : properties) {
            if (MOVINGWALL_PROPERTYNAME.equalsIgnoreCase(property.getTitel())) {
                String value = property.getWert();
                return yearFormat.parse(value);
            }
        }
        throw new IllegalArgumentException("no movingWall property found");
    }

    @Override
    public String cancel() {
        return null;
    }

    @Override
    public String finish() {
        return null;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public Step getStep() {
        return step;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    @Override
    public void setDelay(long seconds) {
        // do we need setting a new delay ?
        // where to save? - new table in mysql?
    }

    @Override
    public int getRemainingDelay() {
        Date movingWallDate = null;
        try {
            movingWallDate = getMovingWallDate(step);
        } catch (IllegalArgumentException | ParseException e) {
            log.error("error getting the moving wall date", e);
        }
        if (movingWallDate == null) {
            log.error("Cannot check moving wall delay for " + step.getProcessId() + ": No movingwall timestamp found");
            return Integer.MAX_VALUE;
        }
        LocalDate datetime = new LocalDate();
        LocalDate expire = new LocalDate(movingWallDate.getTime());
        if (datetime.isAfter(expire)) {
            return 0;
        } else {
            Days days = Days.daysBetween(datetime, expire);
            return days.getDays();
        }
    }

    @Override
    public boolean delayIsExhausted() {
        Date movingWallDate = null;
        try {
            movingWallDate = getMovingWallDate(step);
        } catch (IllegalArgumentException | ParseException e) {
            log.error("Error getting the delay information", e);
        }
        if (movingWallDate == null) {
            log.error("Cannot exhaust moving wall delay for " + step.getProcessId() + ": No movingwall timestamp found");
            return false;
        }
        LocalDate datetime = new LocalDate();
        LocalDate expire = new LocalDate(movingWallDate.getTime());
        return datetime.isAfter(expire);
    }

    @Override
    public String getPagePath() {
        return null;
    }
}
