package gui.library;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;
import core.item.ItemCls;
import lang.Lang;


public class My_Add_Image_Panel extends JLabel {

	private static final long serialVersionUID = 1L;
	public byte[] imgButes;
	public String image_Label_text;
	public int max_widht;
	private My_Add_Image_Panel th;
	public int max_Height = 0;

	
	public My_Add_Image_Panel(String text, int max_Widht, int max_Height) {
		super();
		th = this;
		this.max_widht = max_Widht;
		this.max_Height = max_Height;
		this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		this.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		this.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		this.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		image_Label_text = text;
		th.setText(image_Label_text);
		th.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == MouseEvent.BUTTON1)
					addimage();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});
		JPopupMenu menu = new JPopupMenu();
		JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Reset"));
		copyAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				th.reset();
			}
		});
		menu.add(copyAddress);
		this.setComponentPopupMenu(menu);
	}

	protected void addimage() {
		// TODO Auto-generated method stub
		// открыть диалог для файла
		My_JFileChooser chooser = new My_JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image", "png", "jpg", "gif");
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(Lang.getInstance().translate("Open Image") + "...");
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
			File file = new File(chooser.getSelectedFile().getPath());
			// если размер больше 30к то не вставляем
			if (file.length() > ItemCls.MAX_IMAGE_LENGTH) {
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("File too Large"),
						Lang.getInstance().translate("File too Large"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// его надо в базу вставлять
			imgButes = null;
			try {
				imgButes = getBytesFromFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStream inputStream = new ByteArrayInputStream(imgButes);
			try {
				BufferedImage image1 = ImageIO.read(inputStream);
				ImageIcon image = new ImageIcon(image1);
				int x = image.getIconWidth();
				int y = image.getIconHeight();
				if (max_Height > 0) {
					int y1 = max_Height;
					double k = ((double) y / (double) y1);
					x = (int) ((double) x / k);
					if (x != 0) {
						Image Im = image.getImage().getScaledInstance(x, y1, 1);
						th.setIcon(new ImageIcon(Im));
						th.setPreferredSize(new Dimension(x, y1));
					}
				} else if (x > max_widht) {
					int x1 = max_widht;
					double k = ((double) x / (double) x1);
					y = (int) ((double) y / k);
					if (y != 0) {
						Image Im = image.getImage().getScaledInstance(x1, y, 1);
						th.setIcon(new ImageIcon(Im));
						th.setPreferredSize(new Dimension(x1, y));
					}
				} else {
					th.setIcon(image);
					th.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (imgButes.length > 0) {
				th.setText("");
			} else {
				th.setText(image_Label_text);
			}
		}
	}

	@SuppressWarnings("resource")
	protected static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		is.close();
		return bytes;
	}
	public void reset(){
		this.imgButes = null;
		this.setIcon(null);
		this.setText(image_Label_text);
	}
}
