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

    public static void main(String[] args) {
        final int PORT = 9999;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur en attente de connexion...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté depuis : " + clientSocket.getInetAddress());

                // Lire le nom de la machine de l'expéditeur et demander confirmation dans le thread principal
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                String senderHostName = dis.readUTF();
                int confirmation = JOptionPane.showConfirmDialog(null, "Voulez-vous recevoir le fichier de " + senderHostName + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    // Gérer la connexion client dans un thread séparé
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

                // Lire le nom du fichier et son extension
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                String fileName = dis.readUTF();
                String extension = dis.readUTF();

                // Créer le fichier avec le nom d'origine et l'extension
                File fileToSave = new File(defaultDownloadFolder, fileName + extension);

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