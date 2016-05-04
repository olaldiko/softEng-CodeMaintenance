package frontend;

import data.Definitions;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Semaphore;

/**
 * MainUI
 * <p>
 * This class will manage the main UI interface. This UI will be initialize by the "Resoruce" class.
 *
 * @author Skynet Team
 */
public class MainUI extends Thread {

    private JFrame window;
    private JTextArea navArea;
    private JTextArea alertArea;

    private JLabel resourceIDLabel, resourceStatusLabel, latitudeLabel, longitudeLabel, serverAddressLabel, serverPortLabel;
    private JLabel totalDistanceLabel, distanceLeftLabel, totalDurationLabel, durationLeftLabel;

    private WebEngine webEngine;
    private JFXPanel panel;

    private Semaphore stop;
    private Semaphore candado;
    private boolean loaded = false;

    private String mapURL = "file:///" + new File("map.html").getAbsolutePath();

    /**
     * Constructor creates the Main Frame and creates the needed semaphores.
     * Additionally, the method creates the Scene (The JavaFX panel that contains the WebView)
     */
    public MainUI() {
        window = new JFrame();
        window.setTitle("Panel de control de Recurso");
        window.setSize(300, 200);
        window.getContentPane().add(createMainPanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setVisible(true);
        createScene();
        stop = new Semaphore(0);
        candado = new Semaphore(1);
    }

    /**
     * At start, the UI sets the JLabels and loads the map in the WebView.
     */
    @Override
    public void run() {
        resetNav();
        addNavText("Esperando ruta...");
        loadURL();
        updateMap(Definitions.latitude, Definitions.longitude);
    }

    /**
     * Main panel is a BorderLayout panel with another 3 panels located in the north, center and south of the UI.
     *
     * @return The main panel of the MainUI
     */
    private Container createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createNorthPanel(), BorderLayout.NORTH);
        panel.add(mapView(), BorderLayout.CENTER);
        panel.add(createSouthPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * The north panel creates all the JLabels to show the information to the user.
     *
     * @return The north panel of the MainUI
     */
    private Container createNorthPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 0, 0));

        panel.add(createResourceIDPanel());
        panel.add(createResourceStatusPanel());
        panel.add(createResourceLocationPanel());
        panel.add(createServerAddressPanel());
        panel.add(createServerPortPanel());
        return panel;
    }

    private Container createResourceIDPanel() {
        JPanel resourceIDPanel = new JPanel(new BorderLayout());
        resourceIDPanel.setBorder(BorderFactory.createTitledBorder("ID del Recurso"));
        resourceIDLabel = new JLabel("--", SwingConstants.CENTER);
        resourceIDPanel.add(resourceIDLabel);
        return resourceIDPanel;
    }

    private Container createResourceStatusPanel() {
        JPanel resourceStatusPanel = new JPanel(new BorderLayout());
        resourceStatusPanel.setBorder(BorderFactory.createTitledBorder("Estado del Recurso"));
        resourceStatusLabel = new JLabel("--", SwingConstants.CENTER);
        resourceStatusPanel.add(resourceStatusLabel);
        return resourceStatusPanel;
    }

    private Container createResourceLocationPanel() {
        JPanel resourceLocationPanel = new JPanel(new BorderLayout());
        resourceLocationPanel.setBorder(BorderFactory.createTitledBorder("Localizacion del Recurso"));
        latitudeLabel = new JLabel("--", SwingConstants.CENTER);
        resourceLocationPanel.add(latitudeLabel, BorderLayout.NORTH);
        longitudeLabel = new JLabel("--", SwingConstants.CENTER);
        resourceLocationPanel.add(longitudeLabel, BorderLayout.CENTER);
        return resourceLocationPanel;
    }

    private Container createServerAddressPanel() {
        JPanel serverAddressPanel = new JPanel(new BorderLayout());
        serverAddressPanel.setBorder(BorderFactory.createTitledBorder("IP del Servidor"));
        serverAddressLabel = new JLabel("--", SwingConstants.CENTER);
        serverAddressPanel.add(serverAddressLabel);
        return serverAddressPanel;
    }

    private Container createServerPortPanel() {
        JPanel serverPortPanel = new JPanel(new BorderLayout());
        serverPortPanel.setBorder(BorderFactory.createTitledBorder("Nº de Puerto"));
        serverPortLabel = new JLabel("--", SwingConstants.CENTER);
        serverPortPanel.add(serverPortLabel);
        return serverPortPanel;
    }
    /**
     * South panel fills the panel (GridLayout) with a combination of panels to show alert and route information.
     *
     * @return The south panel of the MainUI
     */
    private Container createSouthPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 0, 0));
        panel.add(createAlertPanel());
        panel.add(createNavigationPanel());
        return panel;
    }

    private Container createAlertPanel() {
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("Mensajes de alerta"));
        JScrollPane alertScrollPanel = new JScrollPane();
        alertArea = new JTextArea(12, 20);
        alertArea.setEditable(false);
        alertScrollPanel.add(alertArea);
        return alertPanel;
    }

    private Container createNavigationPanel() {
        JScrollPane scrollNav;
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Navegador"));
        navArea = new JTextArea(12, 20);
        scrollNav = new JScrollPane(navArea);
        navArea.setEditable(false);
        navigationPanel.add(createDataPanel(), BorderLayout.NORTH);
        navigationPanel.add(scrollNav);
        return navigationPanel;
    }

    private Container createTotalDistancePanel() {
        JPanel totalDistancePanel = new JPanel(new BorderLayout());
        totalDistancePanel.setBorder(BorderFactory.createTitledBorder("Distancia total"));
        totalDistancePanel.add(totalDistanceLabel = new JLabel("--", SwingConstants.CENTER));
        return totalDistancePanel;
    }

    private Container createTotalDurationPanel() {
        JPanel totalDurationPanel = new JPanel(new BorderLayout());
        totalDurationPanel.setBorder(BorderFactory.createTitledBorder("Duracion total"));
        totalDurationPanel.add(totalDurationLabel = new JLabel("--", SwingConstants.CENTER));
        return totalDurationPanel;
    }

    private Container createRemainingDistancePanel() {
        JPanel remainingDistancePanel = new JPanel(new BorderLayout());
        remainingDistancePanel.setBorder(BorderFactory.createTitledBorder("Distancia restante"));
        remainingDistancePanel.add(distanceLeftLabel = new JLabel("--", SwingConstants.CENTER));
        return remainingDistancePanel;
    }

    private Container createRemainingDurationPanel() {
        JPanel remainingDurationPanel = new JPanel(new BorderLayout());
        remainingDurationPanel.setBorder(BorderFactory.createTitledBorder("Duracion restante"));
        remainingDurationPanel.add(durationLeftLabel = new JLabel("--", SwingConstants.CENTER));
        return remainingDurationPanel;
    }

    private Container createDataPanel() {
        JPanel dataPanel = new JPanel(new GridLayout(1, 4, 0, 0));
        dataPanel.add(createTotalDistancePanel());
        dataPanel.add(createRemainingDistancePanel());
        dataPanel.add(createTotalDurationPanel());
        dataPanel.add(createRemainingDurationPanel());
        return dataPanel;
    }
    /**
     * This method creates the JFXPanel to show the WebView.
     *
     * @return JFXPanel for the JavaFX elements.
     */
    private Component mapView() {
        panel = new JFXPanel();
        return panel;
    }

    /**
     * Creating the Scene means to create and set the WebView into the JFXPanel.
     */
    private void createScene() {
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webEngine = webView.getEngine();
            panel.setScene(new Scene(webView));
        });
    }

    /**
     * This method reloads the map of the WebView.
     */
    public void reload() {
        try {
            candado.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loaded = false;
        candado.release();
        loadURL();
    }

    /**
     * This method loads the map in the WebView. To avoid problems with the Google Maps API of JavaScript,
     * the method uses a listener to unlock a Semaphore when the page is 100% loaded.
     */
    private void loadURL() {
        try {
            candado.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            webEngine.load(mapURL);
            webEngine.getLoadWorker().progressProperty().addListener((arg0, arg1, arg2) -> {
                if (arg2.equals(1.0)) {
                    stop.release();
                    loaded = true;
                }
            });
        });
        candado.release();
    }

    /**
     * The thread that need to interact with the WebView will check if the HTML is completely loaded with this class.
     */
    private void check() {
        if (!loaded) {
            try {
                stop.acquire();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * This method interacts with the HTML web page and adds a marker using a JavaScript function in the HTML file.
     * The funtion sends the command to create the marker to the JavaFX thread.
     *
     * @param lat Double latitude of the marker.
     * @param lng Double longitude of the marker.
     */
    public void addPointer(double lat, double lng) {
        try {
            candado.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        check();
        Platform.runLater(() -> webEngine.executeScript("createMarker(" + lat + ", " + lng + ");"));
        candado.release();
    }

    /**
     * This method interacts with the HTML web page and updates a existing marker using a JavaScript function in the
     * HTML file. The funtion sends the command to update the marker to the JavaFX thread.
     *
     * @param lat Double latitude of the marker.
     * @param lng Double longitude of the marker.
     */
    public void updatePointer(double lat, double lng) {
        try {
            candado.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (loaded) {
            Platform.runLater(() -> webEngine.executeScript("changePositionMarker(" + lat + ", " + lng + ");"));
        }
        candado.release();
    }

    /**
     * This method interacts with the HTML web page and moves the mark to a position using a JavaScript function in
     * the HTML file. The funtion sends the command to move the map to the JavaFX thread.
     *
     * @param lat Double latitude of the marker.
     * @param lng Double longitude of the marker.
     */
    public void updateMap(double lat, double lng) {
        try {
            candado.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        check();
        Platform.runLater(() -> webEngine.executeScript("updateMap(" + lat + ", " + lng + ");"));
        candado.release();
    }

    /**
     * This method adds a text line to the alert text area.
     *
     * @param text String text to write in the text area.
     */
    public void addAlertText(String text) {
        alertArea.append("ALERTA: " + text + "\n");
    }

    /**
     * This method adds a text line to the navigation text area.
     *
     * @param text String text to write in the text area.
     */
    public void addNavText(String text) {
        navArea.append("NAVEGADOR: " + text + "\n");
    }

    /**
     * This method clears the navigation text area.
     */
    public void clearNavText() {
        navArea.setText("");
    }

    public void setID(int id) {
        resourceIDLabel.setText(String.valueOf(id));
    }

    /**
     * Sets the state JLabel depending on the state number.
     *
     * @param estado Integer state number.
     */
    public void setEstado(int estado) {
        if (estado == 0) {
            resourceStatusLabel.setText("LIBRE");
        } else if (estado == 1) {
            resourceStatusLabel.setText("VIAJE IDA");
        } else if (estado == 2) {
            resourceStatusLabel.setText("VIAJE VUELTA");
        }
    }

    public void setLocation(double lat, double lng) {
        latitudeLabel.setText("LAT: " + lat);
        longitudeLabel.setText("LNG: " + lng);
    }

    public void setURL(String url) {
        serverAddressLabel.setText(url);
    }

    public void setSocket(int socket) {
        serverPortLabel.setText(String.valueOf(socket));
    }

    /**
     * Resets all the JLabels at the north panel.
     */
    public void resetNav() {
        totalDurationLabel.setText("--");
        durationLeftLabel.setText("--");
        totalDistanceLabel.setText("--");
        distanceLeftLabel.setText("--");
    }

    public void setDuracionTotal(String text) {
        totalDurationLabel.setText(text);
    }

    public void setDuracionRest(String text) {
        durationLeftLabel.setText(text);
    }

    public void setDistanciaTotal(String text) {
        totalDistanceLabel.setText(text);
    }

    public void setDistanciaRest(String text) {
        distanceLeftLabel.setText(text);
    }

}
