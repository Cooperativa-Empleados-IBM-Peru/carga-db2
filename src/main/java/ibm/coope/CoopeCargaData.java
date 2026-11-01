package ibm.coope;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;

@Data
public class CoopeCargaData {
    
    private String resourcefolder = "./resources";
    private String log4jpropfile = resourcefolder + "/log4j2.properties";
    private Logger logger = null;
    private StopWatch watch;
    Properties prop;
 
    CoopeCargaData() {
 
        System.setProperty("log4j.configurationFile", log4jpropfile);
        logger = LogManager.getLogger(CoopeCargaData.class);

        logger.info("===INICIANDO CARGA DE DATOS COPERATIVA IBM PERU===");
        logger.debug("Iniciando contador (watch) de tiempo");

        watch = new StopWatch();
        watch.start();

    }

    private static final String[] FILE_KEYS = { "coopetxt.maestro", "coopetxt.movimientos", "coopetxt.miscelaneos",
            "coopetxt.aportaciones", "coopetxt.bloqueo", "coopetxt.prestamos", "coopetxt.pagos",
            "coopetxt.certificados", "coopetxt.garantes", "coopetxt.garantizados", "coopetxt.fondoreposicionauto",
            "coopetxt.fondosiniestroauto", "coopetxt.videos" };

    private static final String[] SFTP_KEYS = { "coopesftp.server", "coopesftp.user", "coopesftp.clave",
            "coopesftp.path" };

    private static final String[] DB2_KEYS = { "db2.jdbcurl", "db2.username", "db2.password" };

    /**
     * Loads coopeibm.properties and checks that every property needed by the requested
     * action is defined (and is not a template placeholder).
     *
     * @return false (after logging the reason) when the run must not start
     */
    private boolean loadAndValidateSettings(boolean needsSftp, boolean needsDb2) {

        try {
            prop = CoopeUtils.loadSettings();
        } catch (IllegalStateException e) {
            logger.fatal(e.getMessage());
            return false;
        }

        List<String> required = new ArrayList<>();
        required.add("coopetxt.path");
        required.addAll(List.of(FILE_KEYS));
        if (needsSftp) {
            required.addAll(List.of(SFTP_KEYS));
        }
        if (needsDb2) {
            required.addAll(List.of(DB2_KEYS));
        }

        List<String> missing = CoopeUtils.missingProperties(prop, required.toArray(new String[0]));
        if (!missing.isEmpty()) {
            logger.fatal("Missing or incomplete properties in coopeibm.properties : " + missing);
            return false;
        }

        return true;

    }

    public static void main(String[] args) {

        CoopeCargaData coopecarga = new CoopeCargaData();

        String arg;
        if (args.length == 0) {
            arg = "";
        } else {
            arg = args[0];
        }

        if (!coopecarga.carga(arg)) {
            System.exit(1);
        }

    }

    /** @return false when the run could not start because of a configuration error */
    private boolean carga(String arg) {

        logger.info("main argument : " + arg);

        switch (arg) {
             case "cargadata" :  
                    if (!loadAndValidateSettings(false, true)) {
                        return false;
                    }
                    CoopeCargaFiles cargadata = new CoopeCargaFiles(prop);
                    cargadata.cargaData();
                    break;

            case "getfiles" :   
                    if (!loadAndValidateSettings(true, false)) {
                        return false;
                    }
                    CoopeSftp sftpcoope = new CoopeSftp(prop);
                    sftpcoope.getFiles();
                    break;

            case "fullcargadata" :
                    if (!loadAndValidateSettings(true, true)) {
                        return false;
                    }
                    CoopeSftp fullcargadatasftp = new CoopeSftp(prop);
                    fullcargadatasftp.getFiles();

                   CoopeCargaFiles fullcargadata = new CoopeCargaFiles(prop);
                   fullcargadata.cargaData();

                  break;

            case "FULL" :
                    if (!loadAndValidateSettings(true, true)) {
                        return false;
                    }
                    CoopeSftp fullsftp = new CoopeSftp(prop);
                    fullsftp.getFiles();

                   CoopeCargaFiles fulldata = new CoopeCargaFiles(prop);
                   fulldata.cargaData();

                  break;

            case "help" :  
            default: 
                    String ayuda = "Se aceptan los siguientes parámetros :" + System.getProperty("line.separator")
                        + "  cargadata     : Esta opción cargará los files de datos de la cuentas de los socios a la bd" + System.getProperty("line.separator")
                        + "  fullcargadata : Esta opción treará los files de datos via sftp, y los cargará en la bd" + System.getProperty("line.separator")
                        + "  getfiles      : Esta opción traerá los files de datos via sftp del server Domino" + System.getProperty("line.separator")
                        + "  help          : Mestra esta información de ayuda";

                        System.out.println(ayuda);
                     break;

        }  // switch(arg)

        // Ending routines
        logger.debug("terminando contador (watch) de tiempo");
        watch.stop();
        logger.info("Time Elapsed: " + watch.toString());
        logger.info("---CARGA DE DATOS DE COOPERATIVA IBM PERU TERMINADA---" + System.getProperty("line.separator") );

        return true;

    }

}