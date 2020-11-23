package sketch;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SketchBook {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private JFrame mainFrame;
    private Canvas canvas;
    private BufferStrategy canvasBuffer;
    private Point startPoint;
    private Point endPoint;
    private JPanel toolPanel;
    private Color selectedColor;
    private String selectedMode;
    private String freeLine = "freeLine";
    private String rect = "rect";
    private String oval = "oval";
    private String line = "line";
    private String eraser = "eraser";

    private BufferedImage bufferedImage;
    private Graphics2D canvasGraphics;
    private  Graphics2D saveGraphics;

    public SketchBook() {
        initFrame();

        initCanvas();

        initToolPanel();

        selectedMode = freeLine;
        selectedColor = new Color(0, 0, 0);

        mainFrame.setVisible(true);
    }

    /**
     * 그리기 도구에 대한 설정
     */
    private void initToolPanel() {
        toolPanel = new JPanel();

        JButton colorButton = new JButton("Color");
        colorButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JFrame colorFrame = new JFrame();
                colorFrame.setSize(500, 500);
                colorFrame.setResizable(false);
                colorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                JColorChooser colorChooser = new JColorChooser();
                colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        selectedColor = colorChooser.getColor();
                    }
                });

                colorFrame.add(colorChooser, BorderLayout.CENTER);

                colorFrame.setVisible(true);
                super.mousePressed(e);
            }
        });

        JButton rectButton = new JButton(rect);
        JButton ovalButton = new JButton(oval);
        JButton lineButton = new JButton(line);
        JButton freeLineButton = new JButton(freeLine);
        JButton saveButton = new JButton("Save");
        JButton eraserButton = new JButton(eraser);

        rectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedMode = rect;
            }
        });

        ovalButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedMode = oval;
            }
        });

        freeLineButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedMode = freeLine;
            }
        });

        lineButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedMode = line;
            }
        });

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                save();
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R){
                    selectedMode = rect;
                }
                else if (e.getKeyCode() == KeyEvent.VK_F){
                    selectedMode = freeLine;
                }
                else if (e.getKeyCode() == KeyEvent.VK_S){
                    save();
                }
                else if (e.getKeyCode() == KeyEvent.VK_DELETE){
                    initDrawSetting();
                }
            }
        });

        toolPanel.add(rectButton);
        toolPanel.add(ovalButton);
        toolPanel.add(freeLineButton);
        toolPanel.add(lineButton);
        toolPanel.add(colorButton);
        toolPanel.add(saveButton);

        mainFrame.add(toolPanel, BorderLayout.NORTH);

    }

    /**
     * 메인 프레임 설정
     */
    private void initFrame() {
        mainFrame = new JFrame();
        //창의 크기 조절
        mainFrame.setSize(WIDTH, HEIGHT);
        //창의 크기 변경 가능 조정 => 불가능으로 함
        mainFrame.setResizable(false);
        //창이 꺼질때 프로젝트 종료
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * 그림을 그릴 canvas 설정
     */
    private void initCanvas() {
        canvas = new Canvas();
        canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mainFrame.add(canvas, BorderLayout.CENTER);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!selectedMode.equals(freeLine)) {
                    endPoint = e.getPoint();
                    draw();
                }
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (selectedMode.equals(freeLine)) {
                    startPoint = e.getPoint();
                    draw();
                    endPoint = startPoint;
                }
            }
        });
    }

    /**
     * 마우스 이벤트를 받아서 canvas에 그림을 그리는 역활
     * 저장될 이미지 또한 화면에 그리는 그림과 똑같이 그림
     */
    private void draw() {
        if (Objects.isNull(canvas.getBufferStrategy())) {
            initDrawSetting();
            return;
        }

        canvasGraphics.setPaint(selectedColor);
        saveGraphics.setPaint(selectedColor);

        if (selectedMode.equals(rect)) {
            canvasGraphics.drawRect(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            saveGraphics.drawRect(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
        } else if (selectedMode.equals(oval)) {
            if (endPoint.x > startPoint.x){
                canvasGraphics.drawOval(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
                saveGraphics.drawOval(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            }
            else{
                canvasGraphics.drawOval(startPoint.x- (startPoint.x - endPoint.x), startPoint.y, startPoint.x - endPoint.x, endPoint.y - startPoint.y);
                saveGraphics.drawOval(startPoint.x- (startPoint.x - endPoint.x), startPoint.y, startPoint.x - endPoint.x, endPoint.y - startPoint.y);
            }
        } else if (selectedMode.equals(freeLine)) {
            canvasGraphics.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            saveGraphics.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        } else if (selectedMode.equals(line)) {
            canvasGraphics.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            saveGraphics.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }

        canvasBuffer.show();
    }

    private void save(){
        JFrame fileFrame = new JFrame();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File Location");
        int userSelect = chooser.showSaveDialog(fileFrame);
        if (userSelect == JFileChooser.APPROVE_OPTION){
            File dir = new File(chooser.getSelectedFile().getParent());
            if (!dir.exists()){
                dir.mkdirs();
            }
            try {
                ImageIO.write(bufferedImage, "png", new File(chooser.getSelectedFile().getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 그림을 canvas의 graphics를 받아와서 그 graphics에 그리기위해서 하는 세팅
     */
    private void initDrawSetting(){
        canvas.createBufferStrategy(2);
        canvasBuffer = canvas.getBufferStrategy();
        bufferedImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = (Graphics2D) canvasBuffer.getDrawGraphics();
        saveGraphics = bufferedImage.createGraphics();
        saveGraphics.setBackground(Color.white);
    }
}
