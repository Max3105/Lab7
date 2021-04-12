package bsu.rfe.java;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;
    private static final String SENDER_IP = "127.168.100.5";
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextPane textAreaIncoming;
    private final JTextArea textAreaOutgoing;
    private ArrayList<String> messageHistory = new ArrayList<>();

    public MainFrame() {
        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);
        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextPane();
        textAreaIncoming.setEditable(false);

        //создание гиперссылки
        textAreaIncoming.setContentType("text/html");
        textAreaIncoming.setText("");

        textAreaIncoming.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {

                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    textFieldTo.setText(hyperlinkEvent.getDescription());
                }
            }
        });


        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);
        // Подписи полей
        final JLabel labelFrom = new JLabel("Подпись");
        final JLabel labelTo = new JLabel("Получатель");
        // Поля ввода имени пользователя и адреса получателя
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);
        textFieldTo.setText(SENDER_IP);
        // Текстовая область для ввода сообщения
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0);
        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);
        // Панель ввода сообщения
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        // Кнопка отправки сообщения
        final JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Компоновка элементов панели "Сообщение"
        final GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout2.createSequentialGroup()
                                .addComponent(labelFrom)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldFrom)
                                .addGap(LARGE_GAP)
                                .addComponent(labelTo)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)
                        .addComponent(sendButton))
                .addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelFrom)
                        .addComponent(textFieldFrom)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))
                .addGap(MEDIUM_GAP)
                .addComponent(scrollPaneOutgoing)
                .addGap(MEDIUM_GAP)
                .addComponent(sendButton)
                .addContainerGap());
        // Компоновка элементов фрейма
        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());

        // Создание и запуск потока-обработчика запросов
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    while (!Thread.interrupted()) {
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());
                        // Читаем имя отправителя
                        final String senderName = in.readUTF();
                        // Читаем сообщение
                        final String message = in.readUTF();
                        // Закрываем соединение
                        socket.close();
                        // Выделяем IP-адрес
                        final String address =
                                ((InetSocketAddress) socket
                                        .getRemoteSocketAddress())
                                        .getAddress()
                                        .getHostAddress();
                        // Выводим сообщение в текстовую область
                        StringBuilder reformattedMessage = new StringBuilder();
                        for (int i = 0; i < message.length(); i++) {
                            if(message.charAt(i) == '\n') {
                                reformattedMessage.append("<br>");
                            } else {
                                reformattedMessage.append(message.charAt(i));
                            }
                        }
                        messageHistory.add("<a href = " + address + ">" + senderName + ":</a> " + reformattedMessage);
                        StringBuilder displayMessage = new StringBuilder();
                        for (String someMessage : messageHistory) {
                            displayMessage.append(someMessage).append("<br>");
                        }
                        textAreaIncoming.setText(displayMessage.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка в работе сервера", "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    //серверная часть
    private void sendMessage() {
        try {
            // Получаем необходимые параметры
            final String senderName = textFieldFrom.getText();
            final String destinationAddress = textFieldTo.getText();
            final String message = textAreaOutgoing.getText();
            // Убеждаемся, что поля не пустые
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите имя отправителя", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите адрес узла-получателя", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите текст сообщения", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Создаем сокет для соединения
            final Socket socket = new Socket(destinationAddress, SERVER_PORT);
            // Открываем поток вывода данных
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // Записываем в поток имя
            out.writeUTF(senderName);
            // Записываем в поток сообщение
            out.writeUTF(message);
            // Закрываем сокет
            socket.close();
            // Очищаем текстовую область ввода сообщения
            textAreaOutgoing.setText("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение: узел-адресат не найден",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainFrame frame = new MainFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}


