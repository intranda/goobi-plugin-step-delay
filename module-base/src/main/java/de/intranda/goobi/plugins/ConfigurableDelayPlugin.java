package de.intranda.goobi.plugins;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class ConfigurableDelayPlugin implements IDelayPlugin, IStepPlugin {

    private static final long serialVersionUID = -4052908350317189332L;

    private static final String PLUGIN_NAME = "intranda_delay_configurable";
    private Step step;

    private int delayInDays = 3;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;

        delayInDays = ConfigPlugins.getPluginConfig(PLUGIN_NAME).getInt("delayInDays", 3);
        try {
            SubnodeConfiguration myconfig = ConfigPlugins.getProjectAndStepConfig(PLUGIN_NAME, step);
            delayInDays = myconfig.getInt("delayInDays", delayInDays);
        } catch (Exception e) {
            // no specific configuration was found, so the old style configuration is used
        }
    }

    @Override
    public boolean execute() {
        // set step status to inwork
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);

        Helper.addMessageToProcessJournal(step.getProzess().getId(), LogType.DEBUG, "started delay.", "delay");

        step.setBearbeitungsbeginn(new Date());

        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error("Error while saving the step", e);
        }
        return false;
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
        return null; //NOSONAR
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
        LocalDate startDate = new LocalDate(step.getBearbeitungsbeginn());
        LocalDate destinationDate = startDate.plusDays(delayInDays);

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
        LocalDate destinationDate = startDate.plusDays(delayInDays);

        LocalDate currentDate = new LocalDate();
        return currentDate.isAfter(destinationDate);
    }

    @Override
    public String getPagePath() {
        return null;
    }
}
