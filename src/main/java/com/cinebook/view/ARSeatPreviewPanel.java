package com.cinebook.view;

import com.cinebook.model.Cinema;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Panel for AR seat preview.
 * Provides a 3D-like visualization of the cinema and selected seats.
 */
public class ARSeatPreviewPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JFrame parentFrame;
    private JDialog dialog;
    private List<Seat> selectedSeats;
    private Cinema cinema;
    private String movieTitle;
    
    // Cinema dimensions
    private int totalRows;
    private int seatsPerRow;
    private int totalSeats;
    
    // Preview canvas
    private ARPreviewCanvas previewCanvas;
    
    /**
     * Constructor for ARSeatPreviewPanel.
     * 
     * @param parentFrame The parent frame
     * @param selectedSeats The list of selected seats
     * @param cinema The cinema details
     * @param movieTitle The movie title
     */
    public ARSeatPreviewPanel(JFrame parentFrame, List<Seat> selectedSeats, Cinema cinema, String movieTitle) {
        this.parentFrame = parentFrame;
        this.selectedSeats = selectedSeats != null ? selectedSeats : new ArrayList<>();
        this.cinema = cinema;
        this.movieTitle = movieTitle;
        
        this.totalRows = cinema.getTotalRows();
        this.seatsPerRow = cinema.getSeatsPerRow();
        this.totalSeats = cinema.getTotalSeats();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        initializeUI();
    }
    
    /**
     * Shows the AR seat preview dialog.
     */
    public void showPreview() {
        dialog = new JDialog(parentFrame, "AR Seat Preview - " + movieTitle, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(this);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }
    
    /**
     * Initialize the UI components.
     */
    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("AR Seat Preview - " + movieTitle);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Instruction panel
        JPanel instructionPanel = new JPanel();
        JLabel instructionLabel = new JLabel("Use mouse to rotate the view, scroll to zoom");
        instructionPanel.add(instructionLabel);
        headerPanel.add(instructionPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Preview canvas (center)
        previewCanvas = new ARPreviewCanvas();
        add(previewCanvas, BorderLayout.CENTER);
        
        // Bottom panel with close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close Preview");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        bottomPanel.add(closeButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Canvas for rendering the AR seat preview.
     */
    private class ARPreviewCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        
        // View parameters
        private double viewAngle = 45.0;
        private double zoom = 1.0;
        private int viewOffsetX = 0;
        private int viewOffsetY = 0;
        
        // Animation parameters
        private Timer animationTimer;
        private final int FPS = 30;
        private float animationPhase = 0f;
        
        // Visual elements
        private Color screenColor = new Color(200, 200, 255);
        private Color floorColor = new Color(80, 80, 80);
        private Color seatColor = new Color(100, 100, 100);
        private Color deluxeSeatColor = new Color(150, 50, 50);
        private Color selectedSeatColor = new Color(50, 150, 50);
        private Color wallColor = new Color(50, 50, 50, 128);
        
        /**
         * Constructor for ARPreviewCanvas.
         */
        public ARPreviewCanvas() {
            setBackground(Color.BLACK);
            
            // Mouse interaction for rotating/moving the view
            MouseAdapter3D mouseAdapter = new MouseAdapter3D(this);
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            addMouseWheelListener(mouseAdapter);
            
            // Start animation
            startAnimation();
        }
        
        /**
         * Start the animation timer.
         */
        private void startAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                return;
            }
            
            animationTimer = new Timer(1000 / FPS, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationPhase += 0.02f;
                    if (animationPhase > 2 * Math.PI) {
                        animationPhase -= 2 * Math.PI;
                    }
                    repaint();
                }
            });
            
            animationTimer.start();
        }
        
        /**
         * Stop the animation timer.
         */
        public void stopAnimation() {
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
        
        /**
         * Set the view angle.
         * 
         * @param angle The new view angle in degrees
         */
        public void setViewAngle(double angle) {
            this.viewAngle = angle;
            repaint();
        }
        
        /**
         * Adjust the zoom level.
         * 
         * @param delta The zoom delta to apply
         */
        public void adjustZoom(double delta) {
            this.zoom += delta;
            if (this.zoom < 0.5) this.zoom = 0.5;
            if (this.zoom > 3.0) this.zoom = 3.0;
            repaint();
        }
        
        /**
         * Adjust the view offset.
         * 
         * @param dx The x offset delta
         * @param dy The y offset delta
         */
        public void adjustOffset(int dx, int dy) {
            this.viewOffsetX += dx;
            this.viewOffsetY += dy;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Center point for the projection
            int centerX = width / 2 + viewOffsetX;
            int centerY = height / 2 + viewOffsetY;
            
            // Calculate the cinema dimensions based on the number of seats
            int cinemaWidth = seatsPerRow * 30;
            int cinemaLength = totalRows * 40 + 150; // +150 for screen
            
            // Rotate the view by view angle
            AffineTransform oldTransform = g2d.getTransform();
            g2d.rotate(Math.toRadians(viewAngle), centerX, centerY);
            
            // Apply zoom
            g2d.scale(zoom, zoom);
            
            // Draw cinema floor
            g2d.setColor(floorColor);
            g2d.fillRect(centerX - cinemaWidth / 2, centerY - cinemaLength / 2, cinemaWidth, cinemaLength);
            
            // Draw cinema walls (semi-transparent)
            g2d.setColor(wallColor);
            // Left wall
            g2d.fillRect(centerX - cinemaWidth / 2 - 5, centerY - cinemaLength / 2, 5, cinemaLength);
            // Right wall
            g2d.fillRect(centerX + cinemaWidth / 2, centerY - cinemaLength / 2, 5, cinemaLength);
            // Back wall
            g2d.fillRect(centerX - cinemaWidth / 2 - 5, centerY + cinemaLength / 2, cinemaWidth + 10, 5);
            
            // Draw the screen (at the front)
            g2d.setColor(screenColor);
            int screenY = centerY - cinemaLength / 2 + 50;
            // Create glowing effect
            float glowIntensity = (float) (0.5 + 0.5 * Math.sin(animationPhase));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + 0.5f * glowIntensity));
            g2d.fillRect(centerX - cinemaWidth / 2 + 10, centerY - cinemaLength / 2 + 10, cinemaWidth - 20, 30);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            // Draw "SCREEN" text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Serif", Font.BOLD, 14));
            String screenText = "SCREEN";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(screenText);
            g2d.drawString(screenText, centerX - textWidth / 2, screenY + 20);
            
            // Draw seats
            drawSeats(g2d, centerX, centerY, cinemaWidth, cinemaLength);
            
            // Draw movie title
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Serif", Font.BOLD, 16));
            String titleText = "Now Showing: " + movieTitle;
            textWidth = fm.stringWidth(titleText);
            g2d.drawString(titleText, centerX - textWidth / 2, centerY - cinemaLength / 2 + 70);
            
            // Restore original transform
            g2d.setTransform(oldTransform);
            
            // Draw legend
            drawLegend(g2d, width, height);
        }
        
        /**
         * Draw the cinema seats.
         * 
         * @param g2d The graphics context
         * @param centerX The center X coordinate
         * @param centerY The center Y coordinate
         * @param cinemaWidth The width of the cinema
         * @param cinemaLength The length of the cinema
         */
        private void drawSeats(Graphics2D g2d, int centerX, int centerY, int cinemaWidth, int cinemaLength) {
            // Calculate the starting position for the first row of seats
            int startY = centerY - cinemaLength / 2 + 100; // Start after the screen
            int seatRows = totalRows;
            int seatsInRow = seatsPerRow;
            
            // Calculate seat dimensions
            int seatWidth = 20;
            int seatHeight = 20;
            int seatSpacingX = (cinemaWidth - 20) / seatsInRow;
            int seatSpacingY = 30;
            
            // Draw row by row
            for (int row = 0; row < seatRows; row++) {
                int rowY = startY + row * seatSpacingY;
                
                // Row label (A, B, C, etc.)
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Serif", Font.PLAIN, 12));
                String rowLabel = String.valueOf((char)('A' + row));
                g2d.drawString(rowLabel, centerX - cinemaWidth/2 - 15, rowY + seatHeight/2);
                
                // Draw seats in the row
                for (int col = 0; col < seatsInRow; col++) {
                    int seatX = centerX - cinemaWidth/2 + 10 + col * seatSpacingX;
                    
                    // Determine if this seat is selected
                    String seatNumber = rowLabel + (col + 1);
                    boolean isSelected = isSeatSelected(seatNumber);
                    boolean isDeluxe = (row >= seatRows - 2); // Last two rows are deluxe
                    
                    // Choose seat color
                    if (isSelected) {
                        g2d.setColor(selectedSeatColor);
                    } else if (isDeluxe) {
                        g2d.setColor(deluxeSeatColor);
                    } else {
                        g2d.setColor(seatColor);
                    }
                    
                    // Draw the seat
                    g2d.fillRoundRect(seatX, rowY, seatWidth, seatHeight, 5, 5);
                    
                    // Draw seat number
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Serif", Font.PLAIN, 10));
                    String seatText = String.valueOf(col + 1);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(seatText);
                    g2d.drawString(seatText, seatX + (seatWidth - textWidth) / 2, rowY + seatHeight - 5);
                }
            }
        }
        
        /**
         * Draw the legend for seat types.
         * 
         * @param g2d The graphics context
         * @param width The canvas width
         * @param height The canvas height
         */
        private void drawLegend(Graphics2D g2d, int width, int height) {
            int legendX = 20;
            int legendY = height - 80;
            int boxSize = 15;
            int spacing = 20;
            
            // Regular seat
            g2d.setColor(seatColor);
            g2d.fillRoundRect(legendX, legendY, boxSize, boxSize, 3, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Regular Seat", legendX + boxSize + 5, legendY + boxSize);
            
            // Deluxe seat
            g2d.setColor(deluxeSeatColor);
            g2d.fillRoundRect(legendX, legendY + spacing, boxSize, boxSize, 3, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Deluxe Seat", legendX + boxSize + 5, legendY + spacing + boxSize);
            
            // Selected seat
            g2d.setColor(selectedSeatColor);
            g2d.fillRoundRect(legendX, legendY + spacing * 2, boxSize, boxSize, 3, 3);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Your Selected Seat", legendX + boxSize + 5, legendY + spacing * 2 + boxSize);
        }
        
        /**
         * Check if a seat with the given seat number is in the selected seats list.
         * 
         * @param seatNumber The seat number to check
         * @return True if the seat is selected, false otherwise
         */
        private boolean isSeatSelected(String seatNumber) {
            for (Seat seat : selectedSeats) {
                if (seat.getSeatNumber().equals(seatNumber)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Mouse adapter for 3D view interaction.
     */
    private class MouseAdapter3D extends java.awt.event.MouseAdapter {
        private ARPreviewCanvas canvas;
        private Point lastPoint;
        
        /**
         * Constructor for MouseAdapter3D.
         * 
         * @param canvas The AR preview canvas
         */
        public MouseAdapter3D(ARPreviewCanvas canvas) {
            this.canvas = canvas;
        }
        
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            lastPoint = e.getPoint();
        }
        
        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            Point currentPoint = e.getPoint();
            int dx = currentPoint.x - lastPoint.x;
            
            // Rotate view or adjust offset based on button pressed
            if (SwingUtilities.isLeftMouseButton(e)) {
                // Rotate view
                double angleChange = dx * 0.5; // Adjust sensitivity
                canvas.setViewAngle(canvas.viewAngle + angleChange);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                // Move view
                int dy = currentPoint.y - lastPoint.y;
                canvas.adjustOffset(dx, dy);
            }
            
            lastPoint = currentPoint;
        }
        
        @Override
        public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
            // Zoom in/out
            double zoomDelta = e.getWheelRotation() * -0.1; // Adjust sensitivity
            canvas.adjustZoom(zoomDelta);
        }
    }
}