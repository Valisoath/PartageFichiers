/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import javax.swing.*;

public class Envoyer {

    public void envoieFichier(JTabbedPane jTabbedPane1, JLabel jLabel) {
        final int PORT = 9998; // Port de l'envoyeur
        final int DISCOVERY_PORT = 9997; // Port de découverte
        final int MAX_WAIT_TIME = 20000; // Temporisation maximale en millisecondes

        long startTime = System.currentTimeMillis();
        boolean connected = false; // Variable pour suivre l'état de la connexion

        VerifierConnecter connexionWifi = new VerifierConnecter();
        if (connexionWifi.isConnectedToWiFi()) {
            try (DatagramSocket socket = new DatagramSocket()) {
                // Émission du message de découverte
                System.out.println("Attente d'un récepteur...");

                while (!connected && System.currentTimeMillis() - startTime < MAX_WAIT_TIME) {
                    byte[] requestData = "DISCOVER".getBytes();
                    DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                    socket.send(requestPacket);

                    // Attente de la réponse du récepteur pendant 1 seconde
                    socket.setSoTimeout(1000);

                    byte[] responseData = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

                    try {
                        jLabel.setText("Attente d'un récepteur...");
                        socket.receive(responsePacket);
                        String receiverIP = responsePacket.getAddress().getHostAddress();

                        try (Socket serverSocket = new Socket(receiverIP, PORT)) {
                            // Connexion établie avec le récepteur, envoyer des fichiers
                            sendFiles(serverSocket);
                            jTabbedPane1.setSelectedIndex(5);
                            jLabel.setText("");
                            connected = true; // Indique que la connexion est établie
                            break; // Sortir de la boucle si la connexion est établie
                        }
                    } catch (SocketTimeoutException ignored) {
                        // Ignorer et réessayer
                    }
                }

                // Aucun récepteur connecté après la temporisation maximale
                if (!connected && System.currentTimeMillis() - startTime >= MAX_WAIT_TIME) {
                    JOptionPane.showMessageDialog(null, "Aucun récepteur n'a répondu.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                    jLabel.setText("");
                }

            } catch (IOException e) {
                jLabel.setText("");
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Vérifier votre partage wifi", "Avertissement", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void sendFiles(Socket clientSocket) {
        System.out.println("Connexion établie avec le récepteur, envoi des fichiers...");
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            String senderIP = localAddress.getHostAddress();
            String senderHostName = InetAddress.getByName(senderIP).getHostName();

            File[] selectedFiles = chooseFiles();

            // Créer un fichier temporaire pour stocker les fichiers compressés
            File tempZipFile = File.createTempFile("temp", ".zip");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempZipFile); ZipOutputStream zipOut = new ZipOutputStream(fileOutputStream)) {

                for (File selectedFile : selectedFiles) {
                    try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                        // Obtenir le nom du fichier sans extension
                        String fileName = selectedFile.getName();
                        // Créer une entrée dans le fichier ZIP pour ce fichier
                        zipOut.putNextEntry(new ZipEntry(fileName));
                        // Écrire le contenu du fichier dans le flux zip
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            zipOut.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Fichier compressé avec succès : " + fileName);
                        // Fermer l'entrée ZIP courante
                        zipOut.closeEntry();
                    }
                }
            }

            // Envoyer le fichier ZIP au récepteur
            try (FileInputStream tempFileInputStream = new FileInputStream(tempZipFile)) {
                byte[] tempBuffer = new byte[8192];
                int tempBytesRead;
                OutputStream outputStream = clientSocket.getOutputStream();
                while ((tempBytesRead = tempFileInputStream.read(tempBuffer)) != -1) {
                    outputStream.write(tempBuffer, 0, tempBytesRead);
                }
                System.out.println("Fichier ZIP envoyé avec succès : " + tempZipFile.getName());
            }

            // Supprimer le fichier temporaire après l'envoi
            tempZipFile.delete();

            // Pause pour libérer les ressources réseau
            Thread.sleep(1000);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private int generateRandomCode() {
        return 10000 + (int) (Math.random() * 90000); // Génère un nombre aléatoire entre 10000 et 99999 inclus
    }

    private static File[] chooseFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); // Permettre la sélection de plusieurs fichiers
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFiles();
        } else {
            throw new RuntimeException("Aucun fichier sélectionné !");
        }
    }
}
