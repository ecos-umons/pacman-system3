/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman_infd;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import pacman_infd.Elements.Cherry;
import pacman_infd.Elements.Ghost;
import pacman_infd.Elements.Pellet;
import pacman_infd.Elements.SuperPellet;

/**
 *
 * @author Marinus
 */
public class GameController implements GameEventListener {

    private GameWorld gameWorld;
    private View view;
    private ScorePanel scorePanel;
    private boolean cherrySpawned;
    private GameState gameState;
    private Timer gameTimer;
    private StopWatch stopWatch;
    private ResourceManager resourceManager;

    public GameController(View view, ScorePanel scorePanel) {

        this.view = view;
        this.scorePanel = scorePanel;
        cherrySpawned = false;
        gameState = GameState.PREGAME;
        resourceManager = new ResourceManager();
        
        ActionListener gameTimerAction = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gameTimerActionPerformed(evt);
            }
        };
        
        gameTimer = new Timer(10, gameTimerAction);
        stopWatch = new StopWatch();

    }

    @Override
    public void gameElementPerfomedAction(GameElement e) {
        //drawGame();
        view.requestFocus();
    }

    @Override
    public void pacmanMoved() {

        //drawGame();
        view.requestFocus();
    }

    public void pacmanFoundPellet() {
        scorePanel.addScore(5);
        scorePanel.repaint();
        if (!cherrySpawned) {
            if (gameWorld.countPellets() <= gameWorld.getNumberOfPelletsAtStart() / 2) {
                gameWorld.placeCherryOnRandomEmptyCell();
                cherrySpawned = true;
            }
        }
        if(gameWorld.countPellets() == 0){
            nextLevel();
        }
        //soundManager.playWaka();
    }

    public void pacmanDied() {
        scorePanel.looseLife();
        scorePanel.repaint();
        if (scorePanel.getLives() <= 0) {
            gameOver();
        } else {
            gameWorld.getPacman().resetPacman();
            for (Ghost ghost : gameWorld.getGhosts()) {
                ghost.resetGhost();
            }
        }
    }

    public void pacmanFoundSuperPellet() {
        scorePanel.addScore(50);
        scorePanel.repaint();
        for (Ghost ghost : gameWorld.getGhosts()) {
            ghost.runFromPacman();
        }
    }

    public void pacmanEatsGhost() {
        scorePanel.addScore(500);
        scorePanel.repaint();
    }

    @Override
    public void pacmanChangedState(boolean state) {
        
    }

    private void drawGame() {

        Graphics g = view.getGameWorldGraphics();

        if (g != null && gameWorld != null) {
            gameWorld.draw(g);

            view.drawGameWorld();
        }
    }

    public View getView() {
        return view;
    }

    public void newGame() {
//        if (gameState == GameState.PREGAME) {
            gameWorld = null;
            gameWorld = new GameWorld(this, resourceManager.getFirstLevel());
            scorePanel.resetStats();
            gameState = GameState.RUNNING;
            drawGame();
            gameTimer.start();
            stopWatch.reset();
            stopWatch.start();
//        }
    }
    
    public void nextLevel(){
        gameWorld = null;
        gameWorld = new GameWorld(this, resourceManager.getNextLevel());
    }

    public void pauzeGame() {
        if (gameState == GameState.RUNNING) {
            for (Ghost ghost : gameWorld.getGhosts()) {
                ghost.stopTimer();
            }
            gameWorld.getPacman().stopTimer();
            gameTimer.stop();
            stopWatch.stop();
            gameState = GameState.PAUSED;
        }
        else if (gameState == GameState.PAUSED){
            for (Ghost ghost : gameWorld.getGhosts()) {
                ghost.startTimer();
            }
            gameWorld.getPacman().startTimer();
            gameTimer.start();
            stopWatch.start();
            gameState = GameState.RUNNING;
        }
    }

    public void gameTimerActionPerformed(ActionEvent e) {
        checkCollisions();
        drawGame();
        scorePanel.setTime(stopWatch.getElepsedTimeMinutesSeconds());
        scorePanel.repaint();
        
    }
    
//    public void updateTimerActionPerformed(ActionEvent e){
//        
//        drawGame();
//        
//    }
    
    private void checkCollisions(){
        
        Cell pacCell = gameWorld.getPacman().getCell();
        GameElement gameElement = pacCell.getStaticElement();
        
        if(gameElement instanceof Pellet){
            pacCell.setStaticElement(null);
            pacmanFoundPellet();
        }
        else if(gameElement instanceof SuperPellet) {
            pacCell.setStaticElement(null);
            pacmanFoundSuperPellet();
        }
        else if(gameElement instanceof Cherry){
            pacCell.setStaticElement(null);
            pacmanFoundCherry();
        }
        
        for(GameElement element: pacCell.getElements()){
            if(element instanceof Ghost){
                Ghost ghost = (Ghost)element;
                if (ghost.getState() == Ghost.GhostState.VULNERABLE) {
                    ghost.dead();
                    pacmanEatsGhost();
                } else if (ghost.getState() == Ghost.GhostState.NORMAL){
                    pacmanDied();
                }
                break;
            }
        }
        
 
    }


    public void pacmanFoundCherry() {
        scorePanel.addScore(100);
        scorePanel.repaint();
    }

    private void gameOver() {
        pauzeGame();
        view.repaint();
        drawGame();
        JOptionPane.showMessageDialog(
                null, 
                "Game over!\nYour score: " + scorePanel.getScore(), 
                "Game over!", 
                JOptionPane.ERROR_MESSAGE
        ); 
        gameWorld = null;
        gameState = GameState.PREGAME;
        
//        gameTimer.stop();
//        stopWatch.stop();
    }
    
    public GameState getGameState(){
        return gameState;
    }

    @Override
    public void gameElementMovedToCell(Cell cell) {
        //
    }

}
