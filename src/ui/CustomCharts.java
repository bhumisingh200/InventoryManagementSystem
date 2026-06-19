package ui;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class CustomCharts {

    // Modern color palette for charts
    private static final Color[] CHART_COLORS = {
        new Color(46, 204, 113),  // Emerald Green
        new Color(52, 152, 219),  // Peter River Blue
        new Color(155, 89, 182),  // Amethyst Purple
        new Color(230, 126, 34),  // Carrot Orange
        new Color(241, 196, 15),  // Sunflower Yellow
        new Color(26, 188, 156),  // Turquoise
        new Color(231, 76, 60),   // Alizarin Red
        new Color(52, 73, 94)     // Wet Asphalt
    };

    // 1. DONUT PIE CHART PANEL
    public static final class DonutPieChart extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient Map<String, Integer> data;
        private String title;

        @SuppressWarnings("this-escape")
        public DonutPieChart(String title, Map<String, Integer> data) {
            this.title = title;
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(300, 250));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            // Draw Background Border/Box
            g2.setColor(getBackground());

            // Check if data is empty
            if (data == null || data.isEmpty()) {
                g2.setColor(getForeground());
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("No Sales Data Available", width / 2 - 70, height / 2);
                return;
            }

            // Calculate total
            double total = 0;
            for (int val : data.values()) {
                total += val;
            }

            // Donut geometry
            int diameter = Math.min(width, height) - 80;
            if (diameter < 50) diameter = 50;
            int x = (width - diameter) / 2 - 40; // Shift left to fit legend
            int y = (height - diameter) / 2 + 10;

            double curAngle = 0;
            int colorIdx = 0;

            List<String> keys = new ArrayList<>(data.keySet());
            for (String key : keys) {
                int val = data.get(key);
                if (val <= 0) continue;

                double arcAngle = (val / total) * 360.0;
                g2.setColor(CHART_COLORS[colorIdx % CHART_COLORS.length]);
                g2.fill(new Arc2D.Double(x, y, diameter, diameter, curAngle, arcAngle, Arc2D.PIE));

                curAngle += arcAngle;
                colorIdx++;
            }

            // Draw center cutout for Donut effect (super premium!)
            int cutoutDiameter = (int) (diameter * 0.55);
            int cx = x + (diameter - cutoutDiameter) / 2;
            int cy = y + (diameter - cutoutDiameter) / 2;
            
            // Get parent panel background or default theme background
            Color bg = getParent() != null ? getParent().getBackground() : Color.WHITE;
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Double(cx, cy, cutoutDiameter, cutoutDiameter));

            // Write Total inside Donut
            g2.setColor(getForeground());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("TOTAL", x + diameter/2 - 18, y + diameter/2 - 5);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String totalStr = String.valueOf((int) total);
            g2.drawString(totalStr, x + diameter/2 - (totalStr.length() * 4), y + diameter/2 + 12);

            // Draw Title
            g2.setColor(getForeground());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, 15, 25);

            // Draw Legend
            int legendX = x + diameter + 20;
            int legendY = y + 10;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            colorIdx = 0;
            for (String key : keys) {
                int val = data.get(key);
                if (val <= 0) continue;
                if (legendY > height - 20) break; // prevent overflow

                g2.setColor(CHART_COLORS[colorIdx % CHART_COLORS.length]);
                g2.fill(new RoundRectangle2D.Double(legendX, legendY, 12, 12, 4, 4));

                g2.setColor(getForeground());
                String label = key.length() > 12 ? key.substring(0, 10) + ".." : key;
                g2.drawString(label + " (" + val + ")", legendX + 18, legendY + 10);

                legendY += 20;
                colorIdx++;
            }
        }
    }

    // 2. ROUNDED BAR CHART PANEL
    public static final class RoundedBarChart extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient Map<String, Integer> data;
        private String title;

        @SuppressWarnings("this-escape")
        public RoundedBarChart(String title, Map<String, Integer> data) {
            this.title = title;
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(450, 250));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            // Draw Title
            g2.setColor(getForeground());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, 15, 25);

            if (data == null || data.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("No Data Available", width / 2 - 50, height / 2);
                return;
            }

            // Find max value
            int maxVal = 0;
            for (int val : data.values()) {
                if (val > maxVal) maxVal = val;
            }
            if (maxVal == 0) maxVal = 1;

            int padding = 40;
            int chartHeight = height - padding - 50;
            int startX = 50;
            int chartWidth = width - startX - 30;

            int numBars = data.size();
            int barGap = 15;
            int barWidth = (chartWidth - (barGap * (numBars - 1))) / numBars;
            if (barWidth > 50) barWidth = 50; // clamp width
            if (barWidth < 10) barWidth = 10;

            // Draw axis
            g2.setColor(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), 40));
            g2.drawLine(startX - 10, height - padding, startX + chartWidth, height - padding);

            int i = 0;
            List<String> categories = new ArrayList<>(data.keySet());
            for (String category : categories) {
                int val = data.get(category);
                
                int bh = (int) (((double) val / maxVal) * chartHeight);
                int bx = startX + i * (barWidth + barGap) + (chartWidth - numBars * (barWidth + barGap)) / 2;
                int by = height - padding - bh;

                // Draw Bar with Gradient and Rounded Corners
                GradientPaint gp = new GradientPaint(
                    bx, by, CHART_COLORS[i % CHART_COLORS.length],
                    bx, height - padding, new Color(CHART_COLORS[i % CHART_COLORS.length].getRed(), CHART_COLORS[i % CHART_COLORS.length].getGreen(), CHART_COLORS[i % CHART_COLORS.length].getBlue(), 120)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(bx, by, barWidth, bh, 8, 8));

                // Draw value label
                g2.setColor(getForeground());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String valStr = String.valueOf(val);
                g2.drawString(valStr, bx + (barWidth - valStr.length() * 6) / 2, by - 5);

                // Draw Category label underneath
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = category.length() > 8 ? category.substring(0, 6) + ".." : category;
                g2.drawString(label, bx + (barWidth - label.length() * 5) / 2, height - padding + 18);

                i++;
            }
        }
    }

    // 3. GLOWING AREA LINE GRAPH PANEL
    public static final class GlowingLineGraph extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient List<Double> data;
        private transient List<String> labels;
        private String title;

        @SuppressWarnings("this-escape")
        public GlowingLineGraph(String title, List<Double> data, List<String> labels) {
            this.title = title;
            this.data = data;
            this.labels = labels;
            setOpaque(false);
            setPreferredSize(new Dimension(450, 250));
        }

        public void setData(List<Double> data, List<String> labels) {
            this.data = data;
            this.labels = labels;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            // Draw Title
            g2.setColor(getForeground());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, 15, 25);

            if (data == null || data.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("No Transactions Recorded", width / 2 - 80, height / 2);
                return;
            }

            // Find max value
            double maxVal = 0.0;
            for (double val : data) {
                if (val > maxVal) maxVal = val;
            }
            if (maxVal == 0.0) maxVal = 1.0;

            int padding = 40;
            int chartHeight = height - padding - 60;
            int startX = 60;
            int chartWidth = width - startX - 30;

            int pointsCount = data.size();
            double xInterval = (double) chartWidth / Math.max(1, pointsCount - 1);

            // Draw grid lines
            g2.setColor(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), 15));
            for (int k = 0; k <= 4; k++) {
                int yLine = height - padding - (k * chartHeight / 4);
                g2.drawLine(startX, yLine, startX + chartWidth, yLine);
                String valStr = String.format("₹%.0f", k * maxVal / 4);
                g2.drawString(valStr, 10, yLine + 4);
            }

            // Build path
            Path2D.Double path = new Path2D.Double();
            Path2D.Double areaPath = new Path2D.Double();

            for (int i = 0; i < pointsCount; i++) {
                double val = data.get(i);
                double px = startX + i * xInterval;
                double py = height - padding - ((val / maxVal) * chartHeight);

                if (i == 0) {
                    path.moveTo(px, py);
                    areaPath.moveTo(px, height - padding);
                    areaPath.lineTo(px, py);
                } else {
                    path.lineTo(px, py);
                    areaPath.lineTo(px, py);
                }
                
                if (i == pointsCount - 1) {
                    areaPath.lineTo(px, height - padding);
                    areaPath.closePath();
                }
            }

            // 1. Fill Glowing translucent area under the line
            Color lineColor = new Color(52, 152, 219); // Modern Blue
            GradientPaint areaGp = new GradientPaint(
                startX, height - padding - chartHeight, new Color(52, 152, 219, 80),
                startX, height - padding, new Color(52, 152, 219, 0)
            );
            g2.setPaint(areaGp);
            g2.fill(areaPath);

            // 2. Draw thick connecting line
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            // 3. Draw points with labels
            g2.setStroke(new BasicStroke(1.0f));
            for (int i = 0; i < pointsCount; i++) {
                double val = data.get(i);
                double px = startX + i * xInterval;
                double py = height - padding - ((val / maxVal) * chartHeight);

                // Draw white circle with blue outline
                g2.setColor(lineColor);
                g2.fill(new Ellipse2D.Double(px - 5, py - 5, 10, 10));
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(px - 3, py - 3, 6, 6));

                // Draw X Label
                if (labels != null && i < labels.size()) {
                    g2.setColor(getForeground());
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String label = labels.get(i);
                    g2.drawString(label, (int) (px - (label.length() * 2.5)), height - padding + 18);
                }
            }
        }
    }

    // 4. INTERACTIVE GEOGRAPHIC SALES MAP PANEL
    public static final class InteractiveSalesMap extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient Map<String, Double> data;
        private String title;
        private String hoveredHub = null;
        private String selectedHub = null;

        // Hub definitions
        private static class Hub implements java.io.Serializable {
            private static final long serialVersionUID = 1L;
            String id; // e.g. "North (Delhi)"
            String displayName;
            double relX;
            double relY;
            Color themeColor;

            Hub(String id, String displayName, double relX, double relY, Color color) {
                this.id = id;
                this.displayName = displayName;
                this.relX = relX;
                this.relY = relY;
                this.themeColor = color;
            }
        }

        private final Hub[] hubs = {
            new Hub("North (Delhi)", "Delhi Hub (North)", 0.48, 0.22, new Color(52, 152, 219)),
            new Hub("West (Mumbai)", "Mumbai Hub (West)", 0.26, 0.58, new Color(155, 89, 182)),
            new Hub("South (Bangalore)", "Bangalore Hub (South)", 0.38, 0.85, new Color(46, 204, 113)),
            new Hub("East (Kolkata)", "Kolkata Hub (East)", 0.78, 0.48, new Color(230, 126, 34)),
            new Hub("Central (Hyderabad)", "Hyderabad Hub (Central)", 0.46, 0.62, new Color(241, 196, 15))
        };

        @SuppressWarnings("this-escape")
        public InteractiveSalesMap(String title, Map<String, Double> data) {
            this.title = title;
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(450, 300));

            // Track mouse movement for interactive hovers
            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    String lastHovered = hoveredHub;
                    hoveredHub = null;
                    int x = e.getX();
                    int y = e.getY();
                    int w = getWidth();
                    int h = getHeight();

                    for (Hub hub : hubs) {
                        int hx = (int) (hub.relX * w);
                        int hy = (int) (hub.relY * h);
                        double dist = Math.hypot(x - hx, y - hy);
                        if (dist <= 15) {
                            hoveredHub = hub.id;
                            break;
                        }
                    }

                    if ((lastHovered == null && hoveredHub != null) || 
                        (lastHovered != null && !lastHovered.equals(hoveredHub))) {
                        setCursor(new Cursor(hoveredHub != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedHub = null;
                    int x = e.getX();
                    int y = e.getY();
                    int w = getWidth();
                    int h = getHeight();

                    for (Hub hub : hubs) {
                        int hx = (int) (hub.relX * w);
                        int hy = (int) (hub.relY * h);
                        double dist = Math.hypot(x - hx, y - hy);
                        if (dist <= 15) {
                            selectedHub = hub.id;
                            break;
                        }
                    }
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        public void setData(Map<String, Double> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            // Draw Header
            g2.setColor(getForeground());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, 15, 25);

            // Draw clean grid
            boolean isDark = com.formdev.flatlaf.FlatLaf.isLafDark();
            Color gridColor = isDark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 8);
            g2.setColor(gridColor);
            
            for (int x = 20; x < width; x += 30) {
                g2.drawLine(x, 40, x, height - 10);
            }
            for (int y = 45; y < height; y += 30) {
                g2.drawLine(20, y, width - 20, y);
            }

            // Draw link lines
            Hub central = hubs[4];
            int cx = (int) (central.relX * width);
            int cy = (int) (central.relY * height);

            Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f);
            g2.setStroke(dashed);
            g2.setColor(isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 30));

            for (int i = 0; i < 4; i++) {
                Hub hub = hubs[i];
                int hx = (int) (hub.relX * width);
                int hy = (int) (hub.relY * height);
                g2.drawLine(cx, cy, hx, hy);
            }
            g2.setStroke(new BasicStroke(1.0f));

            // Get total sales
            double totalSales = 0.0;
            if (data != null) {
                for (double val : data.values()) {
                    totalSales += val;
                }
            }
            if (totalSales == 0.0) totalSales = 1.0;

            // Draw hubs
            for (Hub hub : hubs) {
                int hx = (int) (hub.relX * width);
                int hy = (int) (hub.relY * height);
                double hubSales = (data != null && data.containsKey(hub.id)) ? data.get(hub.id) : 0.0;

                double salesRatio = hubSales / totalSales;
                int baseRadius = 6;
                int glowRadius = baseRadius + (int) (salesRatio * 20);
                if (glowRadius > 25) glowRadius = 25;

                Color coreColor = hub.themeColor;
                Color glowColor = new Color(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), 60);

                g2.setColor(glowColor);
                g2.fill(new Ellipse2D.Double(hx - glowRadius, hy - glowRadius, glowRadius * 2, glowRadius * 2));
                
                g2.setColor(new Color(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), 120));
                g2.draw(new Ellipse2D.Double(hx - glowRadius, hy - glowRadius, glowRadius * 2, glowRadius * 2));

                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(hx - baseRadius - 1, hy - baseRadius - 1, (baseRadius + 1) * 2, (baseRadius + 1) * 2));
                g2.setColor(coreColor);
                g2.fill(new Ellipse2D.Double(hx - baseRadius, hy - baseRadius, baseRadius * 2, baseRadius * 2));

                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(getForeground());
                String shortLabel = hub.id.substring(0, 5).toUpperCase();
                g2.drawString(shortLabel, hx - 12, hy - glowRadius - 3);
            }

            // Hovered info box
            String activeHubId = hoveredHub != null ? hoveredHub : selectedHub;
            if (activeHubId != null) {
                Hub activeHub = null;
                for (Hub h : hubs) {
                    if (h.id.equals(activeHubId)) {
                        activeHub = h;
                        break;
                    }
                }

                if (activeHub != null) {
                    double hubSales = (data != null && data.containsKey(activeHub.id)) ? data.get(activeHub.id) : 0.0;
                    double percent = totalSales > 1.0 ? (hubSales / totalSales) * 100.0 : 0.0;
                    
                    int hx = (int) (activeHub.relX * width);
                    int hy = (int) (activeHub.relY * height);

                    int boxW = 160;
                    int boxH = 75;
                    int boxX = hx + 15;
                    int boxY = hy - boxH / 2;

                    if (boxX + boxW > width - 10) boxX = hx - boxW - 15;
                    if (boxY < 35) boxY = 35;
                    if (boxY + boxH > height - 10) boxY = height - boxH - 10;

                    g2.setColor(isDark ? new Color(30, 40, 50, 230) : new Color(255, 255, 255, 230));
                    g2.fill(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 12, 12));
                    
                    g2.setColor(activeHub.themeColor);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 12, 12));
                    g2.setStroke(new BasicStroke(1.0f));

                    g2.setColor(getForeground());
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(activeHub.displayName, boxX + 10, boxY + 18);
                    
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString("Status: Operational (Active)", boxX + 10, boxY + 34);
                    
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString(String.format("Sales: Rs. %.2f", hubSales), boxX + 10, boxY + 52);
                    
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 9));
                    g2.setColor(Color.GRAY);
                    g2.drawString(String.format("Share: %.1f%% of overall sales", percent), boxX + 10, boxY + 66);
                }
            } else {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                g2.setColor(Color.GRAY);
                g2.drawString("💡 Hover mouse over regional hub circles to view sales stats.", 15, height - 12);
            }
        }
    }
}
