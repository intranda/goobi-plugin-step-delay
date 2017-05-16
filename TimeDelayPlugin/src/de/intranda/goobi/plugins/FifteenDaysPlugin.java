package de.intranda.goobi.plugins;

import java.util.Date;
import java.util.HashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@PluginImplementation
public class FifteenDaysPlugin implements IDelayPlugin, IStepPlugin {

    private static final String PLUGIN_NAME = "15 days";
    private static final Logger logger = Logger.getLogger(FifteenDaysPlugin.class);
    private Step step;
//    private String returnPath;

    private static final int DELAY_IN_DAYS = 15;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
//        this.returnPath = returnPath;
    }

    
    public boolean execute() {
        // set step status to inwork
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        LogEntry logEntry = new LogEntry();
        logEntry.setContent( "started 15 days delay.");
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(step.getProzess().getId());
        logEntry.setType(LogType.DEBUG);
        logEntry.setUserName("delay");
        ProcessManager.saveLogEntry(logEntry);

        step.setBearbeitungsbeginn(new Date());

        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public String cancel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String finish() {
        // TODO Auto-generated method stub
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

    
    public String getDescription() {
        return PLUGIN_NAME;
    }

    @Override
    public void setDelay(long seconds) {
        // do we need setting a new delay ?
        // where to save? - new table in mysql?
    }

    @Override
    public int getRemainingDelay() {
        LocalDate startDate = new LocalDate(step.getBearbeitungsbeginn());
        LocalDate destinationDate = startDate.plusDays(DELAY_IN_DAYS);

        LocalDate currentDate = new LocalDate();

        if (currentDate.isAfter(destinationDate)) {
            return 0;
        } else {
            return Days.daysBetween(currentDate, destinationDate).getDays();
        }

    }

    @Override
    public boolean delayIsExhausted() {
        LocalDate startDate = new LocalDate(step.getBearbeitungsbeginn());
        LocalDate destinationDate = startDate.plusDays(DELAY_IN_DAYS);

        LocalDate currentDate = new LocalDate();
        if (currentDate.isAfter(destinationDate)) {
            return true;
        }
        return false;
    }
    
    public String getPagePath() {
        return null;
    }
}
