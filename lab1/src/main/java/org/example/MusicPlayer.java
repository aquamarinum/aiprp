package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javazoom.jl.player.Player;

public class MusicPlayer extends JFrame implements ActionListener {
    private JButton playButton;
    private JButton stopButton;
    private Player audioPlayer;
    private Thread playerThread;
    private boolean isPlaying = false;
    private String musicFilePath;

    public MusicPlayer() {
        super("Music Player");
        initializeUI();
        setupMusicFile();
    }

    private void initializeUI() {
        setLayout(new FlowLayout());
        setSize(300, 100);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        playButton = new JButton("Play");
        stopButton = new JButton("Stop");

        playButton.addActionListener(this);
        stopButton.addActionListener(this);

        add(playButton);
        add(stopButton);

        stopButton.setEnabled(false);
    }

    private void setupMusicFile() {
        String projectRoot = System.getProperty("user.dir");
        musicFilePath = projectRoot + File.separator + "files" + File.separator + "sun.mp3";

        File musicFile = new File(musicFilePath);
        if (!musicFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Файл music.mp3 не найден в папке files!\n" +
                            "Полный путь: " + musicFilePath,
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            playMusic();
        } else if (e.getSource() == stopButton) {
            stopMusic();
        }
    }

    private void playMusic() {
        if (isPlaying) return;

        try {
            File musicFile = new File(musicFilePath);
            if (!musicFile.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Музыкальный файл не найден!\nРазместите файл music.mp3 в папке files.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            playerThread = new Thread(() -> {
                try {
                    isPlaying = true;
                    audioPlayer = new Player(new java.io.FileInputStream(musicFile));
                    playButton.setEnabled(false);
                    stopButton.setEnabled(true);

                    audioPlayer.play();

                    playButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    isPlaying = false;

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MusicPlayer.this,
                            "Ошибка воспроизведения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    isPlaying = false;
                    playButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }
            });

            playerThread.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void stopMusic() {
        if (audioPlayer != null) {
            audioPlayer.close();
            isPlaying = false;
        }
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
        playButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void showPlayer() {
        setVisible(true);
    }

    @Override
    public void dispose() {
        stopMusic();
        super.dispose();
    }
}