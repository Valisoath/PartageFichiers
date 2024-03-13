/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Envoyer {

    public void envoieFichier(JTabbedPane jTabbedPane1, JLabel jLabel) {
        final int PORT = 9998; // Port de l'envoyeur
        final int DISCOVERY_PORT = 9997; // Port de découverte
        final int MAX_WAIT_TIME = 20000; // Temporisation maximale en millisecondes

        long startTime = System.currentTimeMillis();
        boolean connected = false; // Variable pour suivre l'état de la connexion

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
            JOptionPane.showMessageDialog(null, "Vérifier votre partage wifi", "Avertissement", JOptionPane.WARNING_MESSAGE);
            jLabel.setText("");
            e.printStackTrace();
        }
    }

    private void sendFiles(Socket clientSocket) {
        System.out.println("Connexion établie avec le récepteur, envoi des fichiers...");
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            String senderIP = localAddress.getHostAddress();
            String senderHostName = InetAddress.getByName(senderIP).getHostName();

            File selectedFile = chooseFile();
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                // Générer un code aléatoire de 5 chiffres
                try (clientSocket) {
                    // Générer un code aléatoire de 5 chiffres
                    int randomCode = generateRandomCode();
                    // Afficher le code dans une boîte de dialogue
                    JOptionPane.showMessageDialog(null, "Votre code de transfert est : " + randomCode, "Code de transfert", JOptionPane.INFORMATION_MESSAGE);
                    // Envoyer le code au récepteur
                    DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                    dos.writeInt(randomCode);
                    // Envoyer le nom de la machine de l'envoyeur avant le fichier
                    dos.writeUTF(senderHostName);
                    // Obtenir le nom du fichier sans extension
                    String fileName = selectedFile.getName();
                    String extension = "";
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex != -1) {
                        extension = fileName.substring(dotIndex);
                        fileName = fileName.substring(0, dotIndex);
                    }   // Envoyer le nom du fichier et son extension
                    dos.writeUTF(fileName);
                    dos.writeUTF(extension);
                    // Envoyer le contenu du fichier au récepteur
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    OutputStream outputStream = clientSocket.getOutputStream();
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }   System.out.println("Fichier envoyé avec succès !");
                    // Fermeture du socket client après l'envoi du fichier
                }
                
                // Pause pour libérer les ressources réseau
                Thread.sleep(1000);

            } catch (InterruptedException ex) {
                Logger.getLogger(Envoyer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int generateRandomCode() {
        return 10000 + (int) (Math.random() * 90000); // Génère un nombre aléatoire entre 10000 et 99999 inclus
    }

    private static File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            throw new RuntimeException("Aucun fichier sélectionné !");
        }
    }
}