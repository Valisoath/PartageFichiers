/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package partagefichiers;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class Recevoir {

    private static final String DEFAULT_DOWNLOAD_FOLDER = System.getProperty("user.home") + File.separator + "Downloads";
    private static final int PORT = 9998; // Port du récepteur
    private static final int DISCOVERY_PORT = 9997; // Port de découverte

    public static void main(String[] args) {
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
    }

    private static class ClientHandler implements Runnable {

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

                // Lire le code de l'expéditeur et demander confirmation dans le thread principal
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                int randomCode = dis.readInt();
                String senderHostName = dis.readUTF();

                int confirmation = JOptionPane.showConfirmDialog(null, "Voulez-vous recevoir le fichier de " + senderHostName + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    // Saisir le code du récepteur
                    String enteredCodeStr = JOptionPane.showInputDialog(null, "Entrez le code de transfert reçu :", "Confirmation", JOptionPane.PLAIN_MESSAGE);

                    try {
                        int enteredCode = Integer.parseInt(enteredCodeStr.trim());

                        if (enteredCode == randomCode) {
                            // Gérer la connexion client dans un thread séparé
                            receiveFile(clientSocket);
                        } else {
                            JOptionPane.showMessageDialog(null, "Code incorrect. Le transfert est annulé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Code invalide. Le transfert est annulé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
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

        private void receiveFile(Socket clientSocket) {
            try {
                // Lire le nom du fichier et son extension
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                String fileName = dis.readUTF();
                String extension = dis.readUTF();

                // Créer le fichier avec le nom d'origine et l'extension
                File fileToSave = new File(DEFAULT_DOWNLOAD_FOLDER, fileName + extension);

                try (FileOutputStream fileOutputStream = new FileOutputStream(fileToSave)) {
                    byte[] buffer = new byte[8192];
                    InputStream inputStream = clientSocket.getInputStream();
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Fichier reçu avec succès et enregistré à : " + fileToSave.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}