package appMain;

import data.Resource;
import frontend.OptionsUI;
import socket.Dispatcher;
import utils.ConfigFile;

import java.util.concurrent.Semaphore;

/**
 * Principal
 * <p>
 * The objective of this Simulation project is to simulate a resource. To do that, we receive information from sokects,
 * and simulate the navigation to incidents using Google Maps and JSONs.
 *
 * @author Skynet Team
 */
public class Principal {

    private Resource resource;
    private Dispatcher dispatcher;

    private OptionsUI optionsUI;

    public static void main(String args[]) {
        System.out.println("PRINCIPAL: Inicio del programa.");
        // Read the file if exists.
        ConfigFile file = new ConfigFile();
        if (file.check()) {
            file.readAll();
        }
        // Start the program.
        Principal p = new Principal();
        p.start();
        System.out.println("PRINCIPAL: Fin del hilo principal del programa.");
    }

    private void start() {
        initConfiUI();
        initDispatcher();
        initResource();
    }

    /**
     * Starts the OptionsUI window to set the simulation variables.
     */
    private void initConfiUI() {
        Semaphore stop = new Semaphore(0);
        optionsUI = new OptionsUI(stop);
        optionsUI.start();
        try {
            stop.acquire();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Dispatcher starts the whole system of socket communications.
     */
    private void initDispatcher() {
        dispatcher = new Dispatcher();
        dispatcher.start();
    }

    /**
     * Creates the Resource object with the Dispatcher.
     */
    private void initResource() {
        resource = new Resource(dispatcher);
        resource.start();
    }

}
