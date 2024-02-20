package de.intranda.goobi.plugins;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.UGHException;

@PluginImplementation
@Log4j2
@Getter
public class BlockingPeriodExpiryDelayPlugin implements IDelayPlugin, IStepPlugin {

    private static final long serialVersionUID = -6928530233362515252L;

    private String title = "intranda_delay_BlockingPeriodExpiry";

    private PluginGuiType pluginGuiType = PluginGuiType.NONE;

    private PluginType type = PluginType.Step;

    private Step step;
    private Process process;

    @Setter
    private long delay;

    private String metadataTypeToCheck = "RestrictionEndDate";

    @Override
    public String cancel() {
        return null;
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public String finish() {
        return null;
    }

    @Override
    public String getPagePath() {
        return null;
    }

    @Override
    public void initialize(Step step, String arg1) {
        this.step = step;
        process = step.getProzess();
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null; // NOSONAR
    }

    @Override
    public boolean delayIsExhausted() {

        LocalDate today = LocalDate.now();
        LocalDate expiryDate = getExpiryDateFromMetadata();
        if (expiryDate == null) {
            // value does not contain a date
            return true;
        }

        // return check if date in metadata value is before or after today
        return expiryDate.isBefore(today);
    }

    private LocalDate getExpiryDateFromMetadata() {
        LocalDate expiryDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String restrictionDate = null;
        try {
            // open mets file
            Fileformat fileformat = process.readMetadataFile();
            DigitalDocument digDoc = fileformat.getDigitalDocument();
            DocStruct logical = digDoc.getLogicalDocStruct();
            // check for metadata
            for (Metadata md : logical.getAllMetadata()) {
                if (md.getType().getName().equals(metadataTypeToCheck)) {
                    restrictionDate = md.getValue();
                }
            }

        } catch (UGHException | IOException | SwapException e) {
            log.error(e);
        }
        // no restriction configured, continue
        if (StringUtils.isBlank(restrictionDate)) {
            return null;
        }

        try {
            // check if it contains a date (yyyy or yyyy-MM-dd)
            if (restrictionDate.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
                expiryDate = simpleDateFormat.parse(restrictionDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (restrictionDate.matches("\\d\\d\\d\\d")) {
                // if only year is given, use -01-01
                expiryDate = simpleDateFormat.parse(restrictionDate + "-01-01").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (ParseException e) {
            log.error(e);
        }
        return expiryDate;
    }

    @Override
    public int getRemainingDelay() {

        LocalDate today = LocalDate.now();
        LocalDate expiryDate = getExpiryDateFromMetadata();
        if (expiryDate == null) {
            // value does not contain a date
            return 0;
        }
        long days = ChronoUnit.DAYS.between(expiryDate, today);
        return (int) days;
    }

}
