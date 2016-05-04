package data;

import frontend.MainUI;
import simulation.Simulation;
import socket.Buzon;
import socket.Dispatcher;
import socket.Message;
import socket.MulticastManager;
import tasks.SenderRunnable;
import utils.ConfigFile;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Resource
 * <p>
 * Resource is the class that manage all the data of the simulation.
 * NOTE: Setters in this class rewrite config file using "rewrite()" method.
 *
 * @author Skynet Team
 */
public class Resource extends Thread {

    private MainUI mainUI;
    private Dispatcher dispatcher;

    private Buzon<String> simuBuzon;

    private Simulation simulation;

    private ScheduledExecutorService scheduler;

    private MulticastManager multicastManager;

    private volatile boolean stop = false;

    public Resource(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        init();
        mainUI = new MainUI();
        mainUI.start();
        simuBuzon = new Buzon<>(1000);
        prepareSimulator();
        initSimulator();
    }

    /**
     * When the resource thread is running, set's UI variables to be shown to the user.
     * Then creates the task running at the background periodically (LOCATION is send each 10 seconds)
     * Finnally, start's listening to the mailbox that is storaged the messages.
     */
    @Override
    public void run() {
        initThread();
        while (!stop) {
            Message msg = dispatcher.receive();
            switch (msg.getType()) {
                case "ALERT":
                    treatAlertMessage(msg);
                    break;
                case "ROUTE":
                    treatRouteMesasge(msg);
                    break;
                case "IDASSIGN":
                    treatIDAssignMessage(msg);
                    break;
                case "JOIN":
                    treatJoinMessage(msg);
                    break;
                default:
                    break;
            }
        }
    }

    private void initThread() {
        mainUI.setURL(Definitions.socketAddres);
        mainUI.setSocket(Definitions.socketNumber);
        mainUI.setLocation(Definitions.latitude, Definitions.longitude);
        mainUI.setEstado(Definitions.estado);
        mainUI.setID(Definitions.id);
        createTasks();
    }

    private void treatAlertMessage(Message msg) {
        mainUI.addAlertText(msg.getData());
    }

    private void treatRouteMesasge(Message msg) {
        if (simulation != null) {
            if (simulation.getDriving()) {
                mainUI.reload();
                killSimulator();
                prepareSimulator();
                initSimulator();
            }
        }
        try {
            simuBuzon.send(msg.getData());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void treatIDAssignMessage(Message msg) {
        setID(msg.getID());
    }

    private void treatJoinMessage(Message msg) {
        Definitions.multicastGroup = msg.getData();
        if (multicastManager != null) {
            if (multicastManager.isAlive()) {
                multicastManager.close();
            }
        }
        multicastManager = new MulticastManager(dispatcher.getParserBuzon());
        multicastManager.start();
    }

    /**
     * This method initialize the resource asking for an ID to the server. If the ID is storaged in the configuration file,
     * then restores it and set's the resource of the state to 0 if it was waiting or moving to an incident or; if it was
     * going back to the base, sends a state 2 to the server to receive the return home route.
     */
    private void init() {
        if (Definitions.id == -1) {
            dispatcher.send(Definitions.id, "IDREQUEST", " ");
            setID(dispatcher.receive("IDASSIGN").getID());
        }
        dispatcher.send(Definitions.id, "CONNECTED", " ");
        if (Definitions.estado == 2) {
            setEstado(2);
        } else if (Definitions.estado == 1) {
            setEstado(0);
        }
    }

    public int getID() {
        return Definitions.id;
    }

    private void setID(int id) {
        if (mainUI != null) mainUI.setID(id);
        Definitions.id = id;
        rewrite();
    }

    public int getEstado() {
        return Definitions.estado;
    }

    public void setEstado(int estado) {
        if (mainUI != null) mainUI.setEstado(estado);
        Definitions.estado = estado;
        dispatcher.send(Definitions.id, "ESTADO", estado);
        rewrite();
    }

    public double getLatitude() {
        return Definitions.latitude;
    }

    public double getLongitude() {
        return Definitions.longitude;
    }

    public String getLocation() {
        return getLatitude() + "," + getLongitude();
    }

    public void setLocation(double latitude, double longitude) {
        Definitions.latitude = latitude;
        Definitions.longitude = longitude;
        mainUI.setLocation(latitude, longitude);
        rewrite();
    }

    /**
     * This methods creates a periodical runnable method to send specific data to the server periodically.
     */
    private void createTasks() {
        SenderRunnable scheduledTask = new SenderRunnable(this, dispatcher, "LOCATION");
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(scheduledTask, 0, 10, TimeUnit.SECONDS);
    }

    private void prepareSimulator() {
        simulation = new Simulation(mainUI, simuBuzon, this);
    }

    private void initSimulator() {
        simulation.start();
    }

    private void killSimulator() {
        simulation.kill();
    }

    /**
     * Rewrite calls "writeAll()" method from "ConfigFile" to write the data with the variables in "Definitions" class.
     */
    private void rewrite() {
        ConfigFile file = new ConfigFile();
        file.writeAll();
    }

}
