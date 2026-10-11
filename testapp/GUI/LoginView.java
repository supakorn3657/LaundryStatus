package testapp.GUI;

import testapp.Lib.FileLogService;
import testapp.Lib.MachineController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LoginView extends JFrame {

    static final Color PAPER = new Color(0xEB, 0xE4, 0xD8);
    static final Color CARD = new Color(0xFF, 0xFC, 0xF8);
    static final Color INK = new Color(0x1C, 0x19, 0x15);
    static final Color MUTED = new Color(0x7A, 0x71, 0x66);
    static final Color LINE = new Color(0xE3, 0xD9, 0xCC);
    static final Color BRASS = new Color(0x8D, 0x70, 0x44);
    static final Color CREAM = new Color(0xF6, 0xF0, 0xE6);
    static final Color GREEN = new Color(0x1F, 0x6B, 0x45);
    static final Color GREEN_BG = new Color(0xE7, 0xF2, 0xEB);
    static final Color RED = new Color(0x8E, 0x3A, 0x34);
    static final Color RED_BG = new Color(0xF8, 0xEB, 0xE8);
    static final Color YELLOW = new Color(0x8A, 0x64, 0x24);
    static final Color YELLOW_BG = new Color(0xF8, 0xF1, 0xDC);
    static final Color SELECT = new Color(0xFA, 0xF6, 0xEE);
    static final Color HINT = new Color(0xA3, 0x98, 0x8B);
    static final int PAGE_WIDTH = 1080;

    static Font uiFont(int style, int size) {
        Font font = new Font("Leelawadee UI", style, size);
        if (font.canDisplayUpTo("กขคงเริ่มซัก") == -1) {
            return font;
        }
        return new Font("Tahoma", style, size);
    }

    static Font clockFont(int size) {
        Font font = new Font("Consolas", Font.PLAIN, size);
        if (font.canDisplay('0')) {
            return font;
        }
        return new Font("Monospaced", Font.PLAIN, size);
    }

    static void maximize(final JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        });
    }

    static JPanel paper(JComponent column) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(PAPER);
        root.add(column);
        return root;
    }

    static JLabel badge(String text, Color fg, Color bg) {
        JLabel chip = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(uiFont(Font.BOLD, 16));
        chip.setForeground(fg);
        chip.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = chip.getPreferredSize();
        chip.setMaximumSize(new Dimension(pref.width, pref.height));
        return chip;
    }

    static JButton actionButton(String text, final Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(uiFont(Font.BOLD, 20));
        button.setForeground(fg);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        button.setPreferredSize(new Dimension(100, 58));
        return button;
    }

    static JButton ghostButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(LINE);
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(uiFont(Font.BOLD, 20));
        button.setForeground(INK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        button.setPreferredSize(new Dimension(100, 58));
        return button;
    }

    static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(uiFont(Font.PLAIN, 18));
        button.setForeground(BRASS);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        Dimension size = button.getPreferredSize();
        button.setMaximumSize(new Dimension(size.width + 12, 36));
        return button;
    }

    static final class RoundPanel extends JPanel {
        private final int arc;

        RoundPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(58, 42, 22, 16));
            g2.fillRoundRect(2, 4, w - 4, h - 6, arc, arc);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, w - 2, h - 2, arc, arc);
            g2.dispose();
        }
    }

    static final class WasherIcon extends JPanel {
        private final Color color;

        WasherIcon(Color color, int size) {
            this.color = color;
            setOpaque(false);
            Dimension box = new Dimension(size, size);
            setPreferredSize(box);
            setMinimumSize(box);
            setMaximumSize(box);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            g2.setStroke(new BasicStroke(Math.max(1.6f, s / 18f)));
            g2.setColor(color);
            int x = Math.round(s * 4f / 28f);
            int y = Math.round(s * 3f / 28f);
            g2.drawRoundRect(x, y, Math.round(s * 20f / 28f), Math.round(s * 22f / 28f), 8, 8);
            int r = Math.round(s * 5.2f / 28f);
            int cx = s / 2;
            int cy = Math.round(s * 16f / 28f);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            int d = Math.max(3, Math.round(s * 2.2f / 28f));
            int dx = Math.round(s * 8.2f / 28f);
            int dy = Math.round(s * 7.2f / 28f);
            g2.fillOval(dx - d / 2, dy - d / 2, d, d);
            g2.dispose();
        }
    }

    static final String PIN_FILE = "room_pin.txt";

    JTextField roomnumLog = new JTextField();
    JPasswordField pinLog = new JPasswordField();
    JLabel errorLog = new JLabel(" ");
    MachineController controller;
    DashboardView board;

    public LoginView() {
        setTitle("สถานะตู้ซักผ้า");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(PAPER);

        RoundPanel card = new RoundPanel(22);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 28, 28, 28));
        card.setPreferredSize(new Dimension(480, 560));
        card.setMaximumSize(new Dimension(480, 600));

        JLabel title = new JLabel("เข้าสู่ระบบ");
        title.setFont(uiFont(Font.BOLD, 34));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("ครั้งแรกที่เข้า รหัส 4 หลักจะถูกจำไว้กับห้อง");
        sub.setFont(uiFont(Font.PLAIN, 18));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("เลขห้อง");
        label.setFont(uiFont(Font.PLAIN, 18));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        roomnumLog.setFont(uiFont(Font.PLAIN, 22));
        roomnumLog.setForeground(INK);
        roomnumLog.setBackground(Color.WHITE);
        roomnumLog.setCaretColor(INK);
        roomnumLog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD9, 0xCD, 0xB8), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        roomnumLog.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomnumLog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel pinLabel = new JLabel("รหัส 4 หลัก");
        pinLabel.setFont(uiFont(Font.PLAIN, 18));
        pinLabel.setForeground(MUTED);
        pinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pinLog.setFont(uiFont(Font.PLAIN, 22));
        pinLog.setForeground(INK);
        pinLog.setBackground(Color.WHITE);
        pinLog.setCaretColor(INK);
        pinLog.setEchoChar('•');
        pinLog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD9, 0xCD, 0xB8), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pinLog.setAlignmentX(Component.LEFT_ALIGNMENT);
        pinLog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        errorLog.setFont(uiFont(Font.PLAIN, 13));
        errorLog.setForeground(RED);
        errorLog.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton enter = actionButton("เข้าจอสถานะ", INK, CREAM);
        enter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openBoard();
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(label);
        card.add(Box.createVerticalStrut(8));
        card.add(roomnumLog);
        card.add(Box.createVerticalStrut(14));
        card.add(pinLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(pinLog);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLog);
        card.add(Box.createVerticalStrut(10));
        card.add(enter);

        setContentPane(paper(card));
        roomnumLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pinLog.requestFocusInWindow();
            }
        });
        pinLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openBoard();
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controller != null) {
                    controller.shutdown();
                }
            }
        });
        maximize(this);
    }

    void openBoard() {
        String room = roomnumLog.getText().trim();
        String pin = new String(pinLog.getPassword()).trim();
        if (room.isEmpty()) {
            errorLog.setText("กรอกเลขห้องก่อน");
            return;
        }
        if (!pin.matches("\\d{4}")) {
            errorLog.setText("รหัสต้องเป็นตัวเลข 4 หลัก");
            return;
        }
        if (!pinMatches(room, pin)) {
            return;
        }
        errorLog.setText(" ");
        if (controller == null) {
            controller = new MachineController(new FileLogService());
            controller.startTimer();
        }
        if (board == null) {
            board = new DashboardView(this, controller, room);
        } else {
            board.enterRoom(room);
        }
        board.setVisible(true);
        setVisible(false);
    }

    void showForNextRoom() {
        roomnumLog.setText("");
        pinLog.setText("");
        errorLog.setText(" ");
        setVisible(true);
        toFront();
        maximize(this);
        roomnumLog.requestFocusInWindow();
    }

    boolean pinMatches(String room, String pin) {
        String saved;
        try {
            saved = readPin(room);
        } catch (IOException ex) {
            errorLog.setText("อ่านรหัสไม่สำเร็จ");
            return false;
        }
        if (saved == null) {
            if (!writePin(room, pin)) {
                errorLog.setText("บันทึกรหัสไม่สำเร็จ");
                return false;
            }
            return true;
        }
        if (!saved.equals(pin)) {
            errorLog.setText("รหัสไม่ถูกต้อง");
            return false;
        }
        return true;
    }

    String readPin(String room) throws IOException {
        File file = new File(PIN_FILE);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int cut = line.indexOf('=');
                if (cut <= 0) {
                    continue;
                }
                if (line.substring(0, cut).equals(room)) {
                    return line.substring(cut + 1).trim();
                }
            }
        }
        return null;
    }

    boolean writePin(String room, String pin) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PIN_FILE, true))) {
            writer.write(room + "=" + pin);
            writer.newLine();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Font font = uiFont(Font.PLAIN, 14);
                UIManager.put("OptionPane.messageFont", font);
                UIManager.put("OptionPane.buttonFont", font);
                new LoginView().setVisible(true);
            }
        });
    }
}
