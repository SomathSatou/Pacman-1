package fr.univangers.pacman.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import fr.univangers.pacman.model.PositionAgent.Dir;
import fr.univangers.pacman.model.strategy.EscapeStrategy;
import fr.univangers.pacman.model.strategy.NearestAttackStrategy;
import fr.univangers.pacman.model.strategy.PlayerStrategy;
import fr.univangers.pacman.model.strategy.RandomStrategy;

public class PacmanGame extends Game {
	public enum Mode {
		oneplayer,
		twoplayerC,
		twoplayerO
	}
	
	private static final long serialVersionUID = 998416452804755455L;
	public static final int nbVieMax=3;
	
	private Maze maze;
	private int score = 0;
	private List<Agent> pacmans = new ArrayList<>();
	private List<Agent> ghosts = new ArrayList<>();
	private List<Integer> nbViePacmans = new ArrayList<>();
	private int nbTurnVulnerables;
	private int nbFood = 0;
	private int scorePerGhosts = 200;
	private Mode mode;
	
	public int score() {
		return score;
	}
	
	public int getNbViePacman(int vieDuPacman) {
		return nbViePacmans.get(vieDuPacman);
	}
	
	public void setNbViePacman(int vieDuPacman, int newValeur) {	
		nbViePacmans.set(vieDuPacman,newValeur);
	}
	
	private void playSound(String filename) {
		try {
	        AudioInputStream audioIn;
			audioIn = AudioSystem.getAudioInputStream(new File(filename));
	        Clip clip = AudioSystem.getClip();
	        clip.close();
	        clip.open(audioIn);
	        clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PacmanGame(int maxTurn, Maze maze, Mode mode) {
		super(maxTurn);
		this.maze = maze;
		this.mode = mode;
		init();
	}
	
	private void updatePosition() {
		clearPositionPacman();
		for(Agent pacman : pacmans) {
			addPositionPacman(pacman.position());
		}
		clearPositionGhosts();
		for(Agent ghost : ghosts) {
			if(!ghost.isDeath())
				addPositionGhosts(ghost.position());
		}
		notifyViews();
	}
	
	public void movePacmanPlayer1(Dir dir) {
		Agent p1 = pacmans.get(0);
		switch(dir) {
		case EAST:
			p1.goRight();
			break;
		case NORTH:
			p1.goUp();
			break;
		case SOUTH:
			p1.goDown();
			break;
		case WEST:
			p1.goLeft();
			break;
		default:
			break;
		}
	}
	
	public void movePacmanPlayer2(Dir dir) {
		Agent p2 = null;
		if(mode == Mode.twoplayerC)
			p2 = pacmans.get(1);
		else if(mode == Mode.twoplayerO)
			p2 = ghosts.get(0);
		if(p2 == null)
			return;
		switch(dir) {
		case EAST:
			p2.goRight();
			break;
		case NORTH:
			p2.goUp();
			break;
		case SOUTH:
			p2.goDown();
			break;
		case WEST:
			p2.goLeft();
			break;
		default:
			break;
		}	
	}
	
	public void moveAgent(Agent agent) {
		agent.action(positionPacmans(), maze.getWalls());
	}
	
	public void reinitPosition() {
		int index = 0;
		for(PositionAgent position : maze.getPacman_start()) {
			pacmans.get(index++).setPosition(position);
		}
		index = 0;
		for(PositionAgent position : maze.getGhosts_start()) {
			ghosts.get(index++).setPosition(position);
		}
	}
	
	@Override
	public void initializeGame() {
		pacmans.clear();
		int p = 0;
		for(PositionAgent position : maze.getPacman_start()) {
			Agent pacman = new Agent(Agent.Type.PACMAN, position);
			if((p < 1) || (p < 2 && mode == Mode.twoplayerC)) {
				pacman.setStrategy(new PlayerStrategy(), new PlayerStrategy());
				p++;
			}
			else
				pacman.setStrategy(new RandomStrategy(), new RandomStrategy());
			pacmans.add(pacman);
			nbViePacmans.add(nbVieMax);
			
		}
		ghosts.clear();
		for(PositionAgent position : maze.getGhosts_start()) {
			Agent ghost = new Agent(Agent.Type.GHOST, position);
			if(p < 2 && mode == Mode.twoplayerO) {
				ghost.setStrategy(new PlayerStrategy(), new PlayerStrategy());
				p++;
			}
			else
				ghost.setStrategy(new NearestAttackStrategy(), new EscapeStrategy());
			ghosts.add(ghost);
		}
		nbFood = 0;
		for(int x = 0; x < maze.getSizeX(); x++) {
			for(int y = 0; y < maze.getSizeY(); y++) {
				nbFood += maze.isFood(x, y) ? 1 : 0;
			}
		}
		playSound("res/sounds/pacman_beginning.wav");
	}

	@Override
	public void takeTurn() {
		if(nbTurnVulnerables == 0) {
			setGhostsScarred(false);
			for (Agent ghost : ghosts) {
				ghost.stopVulnerability();
			}
			nbTurnVulnerables--;
			scorePerGhosts = 200;
		} 
		if(nbTurnVulnerables > 0) {
			nbTurnVulnerables--;
		}
		for(Agent pacman : pacmans) {
			moveAgent(pacman);
			deadAgents(pacman);
			if(maze.isFood(pacman.position().getX(), pacman.position().getY())) {
				maze.setFood(pacman.position().getX(), pacman.position().getY(), false);
				score += 10;
				nbFood--;
				playSound("res/sounds/pacman_chomp.wav");
			}
			if(maze.isCapsule(pacman.position().getX(), pacman.position().getY())) {
				maze.setCapsule(pacman.position().getX(), pacman.position().getY(), false);
				setGhostsScarred(true);
				pacman.inversion();
				for (Agent ghost : ghosts) {
					ghost.vulnerability();
					
				}
				nbTurnVulnerables = 20;
				score += 50;
				playSound("res/sounds/pacman_eatghost.wav");
			}
		}
		for(Agent ghost : ghosts) {
			moveAgent(ghost);
			deadAgents(ghost);
		}
		isOver();

		updatePosition();
	}

	@Override
	public void gameOver() {
		if(pacmans.isEmpty()) {
			System.out.println("Les fantomes ont gagnée");
		}
		if(nbFood == 0) {
			System.out.println("Les pacmans ont gagnée");
		}
	}
	
	public void lifeAgents() {
		int count=0;
		Iterator<Agent> iter = pacmans.iterator();
		while(iter.hasNext()) {
			Agent pacman = iter.next();
			if(pacman.isDeath()) {
				if (getNbViePacman(count)>0) {
					pacman.vivant();
					setNbViePacman(count,getNbViePacman(count)-1);
					reinitPosition();
				}
				else {
					pacmans.remove(pacman);
				}
			}
			count++;
		}
	}
	
	public void deadAgents(Agent agt) {	
		if(pacmans.contains(agt)) {		
			Agent pacman = agt;
			for(Agent ghost: ghosts) {
				if((ghost.position().getX()==pacman.position().getX())&&(ghost.position().getY()==pacman.position().getY())) {
					if (ghost.isVulnerable()) {
						ghost.mort();
						score += scorePerGhosts;
						scorePerGhosts *= 2;
					} else if (ghost.isLife()) {
						pacman.mort();
						lifeAgents();
						playSound("res/sounds/pacman_death.wav");
					}
				}
			}		
		}
		else {		
			Agent ghost = agt;
			for(Agent pacman: pacmans) {
				if((ghost.position().getX()==pacman.position().getX())&&(ghost.position().getY()==pacman.position().getY())) {
					if (ghost.isVulnerable()) {
						ghost.mort();
						score += scorePerGhosts;
						scorePerGhosts *= 2;
					} else if (ghost.isLife()) {
						pacman.mort();
						lifeAgents();
						playSound("res/sounds/pacman_death.wav");
					}
				}
			}
		}
	}
	
	public void isOver() {
		if(pacmans.isEmpty()) {
			over();
		}
		if(nbFood == 0) {
			over();
		}
	}
	
	
}
