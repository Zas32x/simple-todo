/**
 * Simple-ToDo is a simple todo list application capable of marking and saving todo lists
 *
 * @author  Matti Karjalainen
 * @version 1.0
 * @since   2017-10-02 
 */

package com.zas.simpletodo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Task input dialog class of the application.
 * Used for adding and editing tasks.
 *
 */
public class TaskDialog extends JDialog implements ActionListener, ChangeListener
{
	private static final long serialVersionUID=2;
	private JButton addButton,cancelButton;
	private JTextField inputName;
	private JSpinner inputDay,inputMonth,inputYear;
	private SpinnerNumberModel daySpinnerModel;
	private ApplicationWindow parent;
	private boolean editingExisting;
	private int editingIndex;

	/**
	 * Constructor, initalizes the dialog for the given Application Window
	 * 
	 * @param parent parent ApplicationWindow
	 */
	public TaskDialog(ApplicationWindow parent)
	{
		super(parent,true);
		this.parent=parent;
		editingIndex=-1;
		editingExisting=false;
		
		createGUI("",new Date());
	}

	/**
	 * Constructor, initalizes the dialog for the given Application Window
	 * 
	 * @param parent ApplicationWindow parent ApplicationWindow
	 * @param editingIndex int currently edited todo entry on the main window
	 * @param currentName String name of the edited task
	 * @param currentDate Date date of the edited task
	 */
	public TaskDialog(ApplicationWindow parent,int editingIndex,String currentName,Date currentDate)
	{
		super(parent,true);
		this.parent=parent;
		this.editingIndex=editingIndex;
		editingExisting=true;
		
		createGUI(currentName,currentDate);
	}

	/**
	 * Create all visual elements in the dialog window
	 * 
	 * @param name String name given ready for the user in the input field
	 */
	private void createGUI(String name,Date date)
	{
		
		setTitle(editingExisting?"Edit task":"Add task");
		setSize(400,50);
		
		//main content view that will contain the input field and button container, this allows us to add a border
		JPanel content=new JPanel();
		content.setBorder(new EmptyBorder(10,10,10,10));
		content.setLayout(new BorderLayout());

		//panel for labels
		JPanel labelPanel=new JPanel();
		labelPanel.setLayout(new FlowLayout());

		labelPanel.add(makeLabel("Task name",300));
		inputName=new JTextField();
		inputName.setPreferredSize(new Dimension(300,(int)inputName.getPreferredSize().getHeight()));
		inputName.addActionListener(this);
		inputName.setText(name);
		
		Calendar calendar=new GregorianCalendar();
		calendar.setTime(date);

		//day, maximum set in updateAmountOfDays()
		daySpinnerModel=new SpinnerNumberModel();
		daySpinnerModel.setMinimum(1);
		daySpinnerModel.setStepSize(1);
		labelPanel.add(makeLabel("Day",70));
		inputDay=new JSpinner(daySpinnerModel);
		inputDay.setPreferredSize(new Dimension(70,(int)inputDay.getPreferredSize().getHeight()));
		inputDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));

		//month
		labelPanel.add(makeLabel("Month",70));
		inputMonth=new JSpinner(new SpinnerNumberModel(calendar.get(Calendar.MONTH)+1,1,12,1));
		inputMonth.setPreferredSize(new Dimension(70,(int)inputMonth.getPreferredSize().getHeight()));
		inputMonth.addChangeListener(this);
		
		//year
		labelPanel.add(makeLabel("Year",70));
		inputYear=new JSpinner(new SpinnerNumberModel(calendar.get(Calendar.YEAR),1900,9999,1));
		inputYear.setPreferredSize(new Dimension(70,(int)inputYear.getPreferredSize().getHeight()));
		inputYear.addChangeListener(this);
		
		addButton=new JButton(editingExisting?"OK":"Add");
		addButton.addActionListener(this);
		cancelButton=new JButton("Cancel");
		cancelButton.addActionListener(this);
		
		//panel that contains only the buttons
		JPanel buttonPanel=new JPanel();
		buttonPanel.setBorder(new EmptyBorder(10,0,0,0));
		buttonPanel.setLayout(new GridLayout(1,2));
		buttonPanel.add(addButton);
		buttonPanel.add(cancelButton);

		//panel for input fields
		JPanel inputPanel=new JPanel();
		inputPanel.setLayout(new FlowLayout());
		inputPanel.add(inputName);
		inputPanel.add(inputDay);
		inputPanel.add(inputMonth);
		inputPanel.add(inputYear);

		content.add(labelPanel,BorderLayout.PAGE_START);
		content.add(inputPanel,BorderLayout.CENTER);
		content.add(buttonPanel,BorderLayout.PAGE_END);

		updateAmountOfDays();
		
		add(content);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Action event listener for all buttons in the dialog window from ActionListener class
	 * 
	 * @param event Received event
	 */
	@Override
	public void actionPerformed(ActionEvent event)
	{
		//add button and enter button on input field
		if (event.getSource()==inputName||event.getSource()==addButton)
		{
			if (inputName.getText().trim().isEmpty())
			JOptionPane.showMessageDialog(parent,"Enter a name for the task.","Error",JOptionPane.WARNING_MESSAGE);
			else
			{
				Date taskDate;
				try
				{
					taskDate=new GregorianCalendar((int)inputYear.getValue(),(int)inputMonth.getValue()-1,(int)inputDay.getValue()).getTime();
					
					if (editingExisting)//call main window to accept edit
					parent.eventEditTask(editingIndex,inputName.getText(),taskDate);
					else//call main window to add the task
					parent.eventAddTask(inputName.getText(),taskDate);
				} catch (Exception ex)
				{
					System.out.println(ex.getStackTrace());
					JOptionPane.showMessageDialog(this,"Invalid date.","Error",JOptionPane.WARNING_MESSAGE);
				}

				dispose();
			}
		}
		else //cancel button
		if (event.getSource()==cancelButton)
		{
			//close dialog
			dispose();
		}
	}
	
	/**
	 * Updates and restricts the amount of days in the chosen month
	 */
	private void updateAmountOfDays()
	{
		int daysInMonth=new GregorianCalendar((int)inputYear.getValue(),(int)inputMonth.getValue()-1,1).getActualMaximum(Calendar.DAY_OF_MONTH);
		if (inputDay!=null&&(int)inputDay.getValue()>daysInMonth)
		inputDay.setValue(daysInMonth);
		daySpinnerModel.setMaximum(daysInMonth);
	}

	/**
	 * Change event listener for month and year spinners
	 * This is to keep the amount of days in month correct
	 * 
	 * @param event Received event
	 */
	@Override
	public void stateChanged(ChangeEvent event)
	{
		updateAmountOfDays();
	}
	
	/**
	 * Creates a label with given title and width
	 * @param text String text in the label
	 * @param width int width of the label
	 * @return JLabel generated JLabel
	 */
	private JLabel makeLabel(String text,int width)
	{
		JLabel label=new JLabel(text);
		label.setPreferredSize(new Dimension(width,(int)label.getPreferredSize().getHeight()));
		return label;
	}
}
