/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

public class Recevoir {

    private static final String DEFAULT_DOWNLOAD_FOLDER = System.getProperty("user.home") + File.separator + "Downloads";
    private static final int PORT = 9998; // Port du récepteur
    private static final int DISCOVERY_PORT = 9997; // Port de découverte
    private JLabel fileListLabel; // JLabel pour afficher les fichiers reçus

    public void recevoirFichier(JLabel fileListLabel) {
        VerifierConnecter connexionWifi = new VerifierConnecter();
        this.fileListLabel = fileListLabel;

        if (connexionWifi.isConnectedToWiFi()) {
            try (DatagramSocket serverSocket = new DatagramSocket(DISCOVERY_PORT)) {
                System.out.println("En attente de découverte...");

                // Écoute des messages de découverte sur le port de découverte
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // Répondre au message de découverte avec l'adresse IP du récepteur
                String senderIP = receivePacket.getAddress().getHostAddress();
                byte[] responseData = senderIP.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(responsePacket);

                // Attendre les connexions entrantes sur le port du récepteur
                try (ServerSocket receiverServerSocket = new ServerSocket(PORT)) {
                    System.out.println("En attente de connexion entrante...");
                    Socket clientSocket = receiverServerSocket.accept();
                    System.out.println("Connexion entrante établie avec : " + clientSocket.getInetAddress());

                    // Traiter la connexion entrante dans un thread séparé
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Vérifier votre partage wifi", "Avertissement", JOptionPane.WARNING_MESSAGE);
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Emplacement par défaut pour enregistrer le fichier reçu
                File defaultDownloadFolder = new File(DEFAULT_DOWNLOAD_FOLDER);
                if (!defaultDownloadFolder.exists()) {
                    defaultDownloadFolder.mkdirs();
                }

                StringBuilder fileListText = new StringBuilder();

                try (ZipInputStream zipIn = new ZipInputStream(clientSocket.getInputStream())) {
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        String fileName = entry.getName();
                        File fileToSave = new File(DEFAULT_DOWNLOAD_FOLDER, fileName);
                        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToSave)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = zipIn.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                            System.out.println("Fichier reçu avec succès et enregistré à : " + fileToSave.getAbsolutePath());

                            // Ajouter le nom du fichier à la liste des fichiers reçus
                            fileListText.append(fileName).append("<br><br>");
                        }
                    }
                }

                // Mettre à jour le JLabel avec la liste des fichiers reçus
                SwingUtilities.invokeLater(() -> {
                    fileListLabel.setText("<html>" + fileListText.toString() + "</html>");
                    fileListLabel.revalidate();
                    fileListLabel.repaint();
                });

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}