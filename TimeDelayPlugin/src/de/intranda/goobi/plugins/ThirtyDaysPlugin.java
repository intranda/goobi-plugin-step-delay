package de.intranda.goobi.plugins;

import java.util.Date;
import java.util.HashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.WikiFieldHelper;
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
public class ThirtyDaysPlugin implements IDelayPlugin, IStepPlugin {

    private static final String PLUGIN_NAME = "30 days";
    private static final Logger logger = Logger.getLogger(ThirtyDaysPlugin.class);
    private Step step;
//    private String returnPath;

    private static final int DELAY_IN_DAYS = 30;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
//        this.returnPath = returnPath;
    }

    @Override
    public boolean execute() {
        // set step status to inwork
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);

        ProcessManager.addLogfile(WikiFieldHelper.getWikiMessage(step.getProzess().getWikifield(), "debug", "started 30 days delay."), step.getProzess().getId());

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

    @Override
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

}
