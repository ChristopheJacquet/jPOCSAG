/*
	jPOCSAG, a POCSAG paging decoder written in Java.
    Copyright (C) 2009-2011 Christophe Jacquet, F8FTK.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package jpocsag;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jpocsag.gui.AudioPreview;
import jpocsag.gui.CaptureInfoPanel;
import jpocsag.gui.CaptureRenderer;
import jpocsag.gui.MessageFrame;
import jpocsag.gui.MessageListCellRenderer;
import jpocsag.pocsag.Capture;
import jpocsag.pocsag.Message;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class Main {
	private static LinkedList<Message> messages = new LinkedList<Message>();
	private static final float SAMPLE_RATE = 11025.0F;
	
	private static class MessageListModel implements ListModel {
		
		private List<ListDataListener> dl = new ArrayList<ListDataListener>();
		
		public void addListDataListener(ListDataListener l) {
			dl.add(l);
		}

		public Object getElementAt(int index) {
			return messages.get(index);
		}

		public int getSize() {
			return messages.size();
		}

		public void removeListDataListener(ListDataListener l) {
			dl.remove(l);
		}
		
		public void notifyChange() {
			for(ListDataListener l : dl) {
				l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, messages.size()-1));
			}
		}
		
	}
	
	public static void main(String[] args) {
		// Application name for MacOS X
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "jPOCSAG" );
		
		for(Mixer.Info mi : AudioSystem.getMixerInfo()) {
			System.out.println("mixer: " + mi);
		}
		
		
		// Using PCM 44.1 kHz, 16 bit signed,stereo.
		AudioFormat	audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 8, 1, 1, SAMPLE_RATE, false);

		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat, 40000);

		System.out.println("Min buffer size: " + info.getMinBufferSize());
		System.out.println("Max buffer size: " + info.getMaxBufferSize());
		
		TargetDataLine	targetDataLine = null;

		try
		{
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			System.out.println("unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Data line format: " + targetDataLine.getFormat());
		System.out.println("Data line info: " + targetDataLine.getLineInfo());
		
		targetDataLine.start();
		
		byte[] data = new byte[8000];
		SampleBlock sample = null;
		
		final JFrame msgFrame = new JFrame("POCSAG Messages");
		final MessageListModel mlm = new MessageListModel();
		final JList lstMsg = new JList(mlm);
		lstMsg.setCellRenderer(new MessageListCellRenderer());
		
		final MessageFrame messageFrame = new MessageFrame();
		lstMsg.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				messageFrame.setMessage((Message)mlm.getElementAt(lstMsg.getSelectedIndex()));
			}
			
		});
		JButton btnXMLSave = new JButton("Save to XML");
		btnXMLSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
					DOMImplementation impl = builder.getDOMImplementation();
					Document doc = impl.createDocument(null, "session", null);
					Element xmlSession = doc.getDocumentElement();
						//doc.createElement("session");
					for(Message m : messages) {
						xmlSession.appendChild(m.xml(doc));
					}
					
				    DOMSource domSource = new DOMSource(doc);
			        TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			        transformer.setOutputProperty
			            (OutputKeys.OMIT_XML_DECLARATION, "yes");
			        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			        transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			        transformer.setOutputProperty
			            ("{http://xml.apache.org/xslt}indent-amount", "4");
			        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			        StreamResult sr = new StreamResult(new File("/tmp/pocsag.xml"));
			        transformer.transform(domSource, sr);

				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				} catch (TransformerConfigurationException e2) {
					e2.printStackTrace();
				} catch (TransformerException e3) {
					e3.printStackTrace();
				}
				
			}
		});
		
		JButton btnQuit = new JButton("Quit");
		btnQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		msgFrame.setLayout(new BorderLayout());
		JPanel pnl1 = new JPanel(new BorderLayout());
		msgFrame.add(pnl1, BorderLayout.CENTER);
		
		msgFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Button bar
		JPanel buttonBar = new JPanel(new FlowLayout());
		buttonBar.add(btnXMLSave);
		buttonBar.add(btnQuit);
		msgFrame.add(buttonBar, BorderLayout.NORTH);
		
		AudioPreview preview = new AudioPreview();
		JPanel pnl2 = new JPanel(new BorderLayout());
		pnl1.add(preview, BorderLayout.NORTH);
		pnl1.add(pnl2, BorderLayout.CENTER);
		
		CaptureRenderer captureRenderer = new CaptureRenderer();
		CaptureInfoPanel pnlInfo = new CaptureInfoPanel(100, 140);
		
		JPanel pnl3 = new JPanel(new BorderLayout());
		pnl3.add(
				new JScrollPane(
						captureRenderer, 
						JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), 
					BorderLayout.CENTER);
		pnl3.add(pnlInfo, BorderLayout.WEST);
		
		pnl2.add(pnl3, BorderLayout.NORTH);
		
		pnl2.add(new JScrollPane(lstMsg), BorderLayout.CENTER);
		
		
		msgFrame.setPreferredSize(new Dimension(1200, 700));
		msgFrame.pack();
		msgFrame.setVisible(true);

		
		final int averagingSize = (int) (200 * data.length / SAMPLE_RATE);
		byte averages[] = new byte[data.length / averagingSize + 1];
		//double val = 0;
		
		final int SQUELCH_LEVEL = 10; 
		
		for(;;) {
			int num = targetDataLine.read(data, 0, data.length);
			int nonZero = 0;
			//int firstIndexSquelchOpen = -1;
			//int lastIndexSquelchOpen = -1;
			//System.out.print(num + "samples: ");
			//byte zero = data[0];
			Arrays.fill(averages, (byte)0);
			byte averageIndex = 0;
			for(int i=1; i<num; i++) {
				if(data[i] - data[i-1] > SQUELCH_LEVEL) {
					nonZero++;
					//lastIndexSquelchOpen = i;
					//if(firstIndexSquelchOpen == -1) firstIndexSquelchOpen = i;
				}
				if(i % averagingSize == 0) averageIndex++;
				//averages[averageIndex] = (byte)val;
				//val += .0001;
				if(data[i]/6 > averages[averageIndex]) averages[averageIndex] = (byte)(data[i]/6);
				//if(i<20) System.out.print(data[i] + " ");
			}
			preview.addSamples(averages);
			
			if(nonZero > 20) {
				System.out.print("NZ=" + nonZero + "   ");
				if(sample == null) sample = new SampleBlock();
				sample.addData(data); //, firstIndexSquelchOpen, lastIndexSquelchOpen);
			} else {
				if(sample != null) {
					// processing
					int[] smpl = sample.getWholeData();
					
					Capture capture = new Capture(smpl, SAMPLE_RATE, pnlInfo);
					captureRenderer.setCapture(capture);
					
					System.out.println("Freq.:" + capture.periodInSamplesToFrequency(capture.getPeriodInSamples()));
					List<Message> msgs = capture.calculateSamplingTicks(audioFormat.getFrameRate()/600.);
					if(msgs != null) {
						messages.addAll(0, msgs);
						mlm.notifyChange();
					}
					
					pnlInfo.repaint();
					
					sample = null;
				}
			}
		}
		
	}
}
