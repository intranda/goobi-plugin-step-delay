package de.intranda.goobi.plugins;

import java.util.Date;
import java.util.HashMap;

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
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j
public class Year2014Plugin implements IDelayPlugin, IStepPlugin {

    private static final String PLUGIN_NAME = "intranda_delay_year_2014";
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
        
        LogEntry logEntry = new LogEntry();
        logEntry.setContent("Process is blocked until 1.1.2014.");
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(step.getProzess().getId());
        logEntry.setType(LogType.DEBUG);
        logEntry.setUserName("delay");
        ProcessManager.saveLogEntry(logEntry);
        
        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error("Error while saving the step", e);
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
        LocalDate datetime = new LocalDate();
        if (datetime.getYear() > 2013) {
            return 0;
        } else {
            LocalDate newYear = datetime.plusYears(1).withDayOfYear(1);
            Days days = Days.daysBetween(datetime, newYear);
            return days.getDays();
        }
    }

    @Override
    public boolean delayIsExhausted() {
        LocalDate datetime = new LocalDate();
        if (datetime.getYear() > 2013) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        Year2014Plugin plugin = new Year2014Plugin();
        System.out.println(plugin.getRemainingDelay());

    }
    
    public String getPagePath() {
        return null;
    }
}
