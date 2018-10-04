package fr.univangers.pacman.view;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import fr.univangers.pacman.controller.GameController;
import fr.univangers.pacman.model.Game;

public class ViewCommande extends JFrame implements View {

	private static final long serialVersionUID = -1656969420461892798L;
	private Game game;
	private GameController gameController;
	
	private JPanel panelBtn;
	private JButton btnRestart;
	private JButton btnRun;
	private JButton btnStep;
	private JButton btnPause;
	
	private JPanel panelInfo;
	private JPanel panelTime;
	private JSlider sliderTime;
	private JLabel labelNbTurn;

	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}
	
	public ViewCommande(Game game) {
		super();
		this.game = game;
		this.game.addView(this);
		
        setTitle("Commande");
        setSize(new Dimension(700, 300));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2,1));

        panelBtn = new JPanel();
        panelBtn.setLayout(new GridLayout(1,4));
        btnRestart = new JButton(new ImageIcon("res/icons/icon_restart.png"));
        btnRestart.addActionListener(evenement -> { 
        	gameController.restart();
        	btnRestart.setEnabled(false);
        });
        btnRestart.setEnabled(false);
        panelBtn.add(btnRestart);
        btnRun = new JButton(new ImageIcon("res/icons/icon_run.png"));
        btnRun.addActionListener(evenement -> { 
        	gameController.run(); 
        	btnRun.setEnabled(false);
        	btnPause.setEnabled(true);
            btnStep.setEnabled(false);
        	btnRestart.setEnabled(false);
        });
        panelBtn.add(btnRun);
        btnStep = new JButton(new ImageIcon("res/icons/icon_step.png"));
        btnStep.addActionListener(evenement -> { 
        	gameController.step();
        	btnRestart.setEnabled(true);
        });
        btnStep.setEnabled(false);
        panelBtn.add(btnStep);
        btnPause = new JButton(new ImageIcon("res/icons/icon_pause.png"));
        btnPause.addActionListener(evenement -> { 
        	gameController.pause();
        	btnRun.setEnabled(true);
        	btnPause.setEnabled(false);
            btnStep.setEnabled(true);
        	btnRestart.setEnabled(true);
        });
        btnPause.setEnabled(false);
        panelBtn.add(btnPause);
        add(panelBtn);
        
        panelInfo = new JPanel();
        panelTime = new JPanel();
        panelTime.setLayout(new GridLayout(2,1));
        JLabel labelTime = new JLabel("Numbers of turns per second", SwingConstants.CENTER);
        panelTime.add(labelTime);
        sliderTime = new JSlider(1,10,1);
        sliderTime.setMajorTickSpacing(1);
        sliderTime.setMinorTickSpacing(1);
        sliderTime.setPaintTicks(true);
        sliderTime.setPaintLabels(true);
        sliderTime.addChangeListener(evenement -> gameController.setTime(1000/sliderTime.getValue()) );
        panelTime.add(sliderTime);
        panelInfo.add(panelTime);
        panelInfo.setLayout(new GridLayout(1,2));
        labelNbTurn = new JLabel("Turn : " + game.nbTurn(), SwingConstants.CENTER);
        panelInfo.add(labelNbTurn);
        add(panelInfo);
        
		setVisible(true);
	}
	
	
	@Override
	public void update() {
		labelNbTurn.setText("Turn : " + game.nbTurn());
	}

}