/**
 * Simple-ToDo is a simple todo list application capable of marking and saving todo lists
 * <p>
 * This application uses a very simple way to store it's data because it was fast to make in a few hours.
 * Other planned features could have been to automatically open last used file upon startup, shortcuts, menu for stuff like removing completed tasks etc.
 *
 * @author  Matti Karjalainen
 * @version 1.0
 * @since   2017-10-02 
 */

package com.zas.simpletodo;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Main window class of the application.
 * <p>
 * This class handles all the data and interaction of the todo list.
 *
 */
public class ApplicationWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID=1;
	private static final Object[] columnNames={"Done","Task","Date"};
	private static final DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
	private static final int CHECK_COLUMN=0,NAME_COLUMN=1,DATE_COLUMN=2;
	
	private DefaultTableModel todoTableModel;
	private JTable todoTable;
	private JButton addButton,saveButton,loadButton,deleteButton,editButton;
	
	/**
	  * Constructor, intializes everything 
	  */
	public ApplicationWindow()
	{
		super();
		
		createGUI();
	}
	
	/**
	 * Create all visual elements in the main window
	 */
	private void createGUI()
	{
		setWindowTitle(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(460,660);
		setLayout(new BorderLayout());
		
		//scroll pane containing the visible todo table
		JScrollPane scroll=new JScrollPane();
		
		//create todo table
		todoTableModel=new DefaultTableModel(columnNames,0)
		{
			//only allow editing of checked state, rest will be done through the edit button
			@Override
			public boolean isCellEditable(int row,int column)
			{
				return (column==CHECK_COLUMN);
			}
		};
		todoTable=new JTable(todoTableModel)
		{
			//define types of columns to create check boxes and date selection
			@Override
			public Class getColumnClass(int column)
			{
				switch (column)
				{
					default:
					case NAME_COLUMN:
						return String.class;
					case CHECK_COLUMN:
						return Boolean.class;
					case DATE_COLUMN:
						return Date.class;
				}
			}
		};
		todoTable.setModel(todoTableModel);
		todoTable.getColumnModel().getColumn(CHECK_COLUMN).setPreferredWidth(50);
		todoTable.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(500);
		todoTable.getColumnModel().getColumn(DATE_COLUMN).setPreferredWidth(110);
		todoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			//add listener so we can disable and enable the delete and edit task buttons
			@Override
			public void valueChanged(ListSelectionEvent event)
			{
				eventListSelection();
			}
		});
		
		scroll.setViewportView(todoTable);
		todoTable.setFillsViewportHeight(true);

		//generate tool bar and buttons
		JToolBar toolbar=new JToolBar();
		addButton=makeToolBarButton("Add task","resources/add.png");
		saveButton=makeToolBarButton("Save task list","resources/save.png");
		loadButton=makeToolBarButton("Load task list","resources/load.png");
		deleteButton=makeToolBarButton("Delete task","resources/delete.png");
		deleteButton.setEnabled(false);
		editButton=makeToolBarButton("Edit task","resources/edit.png");
		editButton.setEnabled(false);
		toolbar.add(saveButton);
		toolbar.add(loadButton);
		toolbar.add(addButton);
		toolbar.add(editButton);
		toolbar.add(deleteButton);

		add(toolbar,BorderLayout.PAGE_START);
		add(scroll,BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Action event listener for all buttons in the main window from ActionListener class
	 * 
	 * @param event Received event
	 */
	@Override
	public void actionPerformed(ActionEvent event)
	{
		//this is done using an if chain since event.getSource() is not accepted by a switch case
		
		//Add button event
		if (event.getSource()==addButton)
		{
			//open add dialog
			new TaskDialog(this);
		}
		else //delete button event
		if (event.getSource()==deleteButton)
		{
			int selections=todoTable.getSelectedRowCount();
			if (selections>0) //make sure todo tasks are checked even if this shouldn't be possible otherwise
			{
				Object[] options={"Yes","No"};
				int option=JOptionPane.showOptionDialog(this,"Delete selected "+(selections==1?"task":"tasks")+"?","Delete "+(selections==1?"task":"tasks"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,"No");
				
				if (option==0)//"Yes" option
				{
					try
					{
						//iterate and delete rows
						int[] rows=todoTable.getSelectedRows();
						for(int i=rows.length-1;i>=0;i--)
						todoTableModel.removeRow(rows[i]);
					} catch (Exception ex)
					{
						System.out.println(ex.getStackTrace());
						JOptionPane.showMessageDialog(this,"Unable to delete row.","Error",JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}
		else //edit button event
		if (event.getSource()==editButton)
		{
			int selections=todoTable.getSelectedRowCount();
			if (selections==1) //check that only one task is selected, open edit dialog
			new TaskDialog(this,todoTable.getSelectedRow(),(String)todoTable.getValueAt(todoTable.getSelectedRow(),NAME_COLUMN),(Date)todoTable.getValueAt(todoTable.getSelectedRow(),DATE_COLUMN));
		}
		else //load button event
		if (event.getSource()==loadButton)
		{
			JFileChooser chooser=new JFileChooser();
			try
			{
				//open file chooser
				chooser.setFileFilter(new FileNameExtensionFilter("Simple ToDo files","todo"));
				int result=chooser.showOpenDialog(this);
				
				if (result==JFileChooser.APPROVE_OPTION)
				loadFile(chooser.getSelectedFile());
			} catch (Exception ex)
			{
				System.out.println(ex.getStackTrace());
				JOptionPane.showMessageDialog(this,"Unable read file system.","Error",JOptionPane.WARNING_MESSAGE);
			}
		}
		else //save button event
		if (event.getSource()==saveButton)
		{
			JFileChooser chooser=new JFileChooser();
			try
			{
				//open file chooser
				chooser.setFileFilter(new FileNameExtensionFilter("Simple ToDo files","todo"));
				int result=chooser.showSaveDialog(this);
				
				if (result==JFileChooser.APPROVE_OPTION)
				saveFile(chooser.getSelectedFile());
			} catch (Exception ex)
			{
				System.out.println(ex);
				JOptionPane.showMessageDialog(this,"Unable read file system.","Error",JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * Loads the saved todo list file
	 * <p>
	 * File format is as follows: First line contains 'f' or 't' for true and false to store if the task is checked, next line contains the name of the task and next the date.
	 * After this is the checked value of the next task and then it's name. A very simple way to store data.
	 * 
	 * @param file File to open
	 */
	private void loadFile(File file)
	{
		BufferedReader reader;
		
		try
		{
			//initialize list to put file data into
			ArrayList<Object[]> readData=new ArrayList<Object[]>();
			
			reader=new BufferedReader(new FileReader(file));
			
			//stores the checked value of the current task
			String currentChecked="";
			String currentName="";
			String currentDate="";
			
			//loop all lines of the file
			//read 3 lines at a time (checked, name, date)
			while ((currentChecked=reader.readLine())!=null)
			{
				if ((currentName=reader.readLine())==null)
				{
					reader.close();
					throw new Exception("File format does not match");
				}
				if ((currentDate=reader.readLine())==null)
				{
					reader.close();
					throw new Exception("File format does not match");
				}
				
				Object[] newTask={false,"",""};
				
				//get checked state
				if (currentChecked.equals("t"))
				newTask[CHECK_COLUMN]=true;
				else if (currentChecked.equals("f"))
				newTask[CHECK_COLUMN]=false;
				else
				{
					reader.close();
					throw new Exception("File format does not match");
				}
				
				//get name
				newTask[NAME_COLUMN]=currentName;
				
				//get date
				try
				{
					newTask[DATE_COLUMN]=dateFormat.parse(currentDate);
				} catch (Exception ex)
				{
					reader.close();
					throw new Exception("File format does not match");
				}
				
				//add task to read data list
				readData.add(newTask);
			}

			reader.close();
			
			//convert ArrayList to an array
			Object[][] newData=new Object[readData.size()][3];
			for(int i=0;i<readData.size();i++)
			newData[i]=readData.get(i);

			//store current column widths as swing tends to reset it upon changing data vector
			int col1Width=todoTable.getColumnModel().getColumn(CHECK_COLUMN).getPreferredWidth();
			int col2Width=todoTable.getColumnModel().getColumn(NAME_COLUMN).getPreferredWidth();
			int col3Width=todoTable.getColumnModel().getColumn(DATE_COLUMN).getPreferredWidth();
			
			//insert data received from the file into the table
			todoTableModel.setDataVector(newData,columnNames);
			
			//restore previous column widths
			todoTable.getColumnModel().getColumn(CHECK_COLUMN).setPreferredWidth(col1Width);
			todoTable.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(col2Width);
			todoTable.getColumnModel().getColumn(DATE_COLUMN).setPreferredWidth(col3Width);
			
			setWindowTitle(file.getName());
		} catch (Exception ex)
		{
			System.out.println(ex);
			JOptionPane.showMessageDialog(this,"Unable to load file.","Error",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Saves the current todo list to a file
	 * <p>
	 * @see #loadFile(File) for file format
	 * 
	 * @param file
	 */
	private void saveFile(File file)
	{
		BufferedWriter writer;
		
		//add ".todo" to end of file name if missing
		if (!file.getName().toLowerCase().endsWith(".todo"))
		file=new File(file.getAbsolutePath()+".todo");
		
		try
		{
			writer=new BufferedWriter(new FileWriter(file));
			
			//iterate through all entries in todo table
			for(int i=0;i<todoTableModel.getRowCount();i++)
			{
				//add 't' or 'f' for checked state on one line, name on the next line and date on the third line
				String output=((Boolean)todoTableModel.getValueAt(i,CHECK_COLUMN)?"t\n":"f\n")+(String)todoTableModel.getValueAt(i,NAME_COLUMN)+"\n"+dateFormat.format((Date)todoTableModel.getValueAt(i,DATE_COLUMN));
				
				//add a new line after tasks always except for the last task, no new line at the end of file
				if (i<todoTableModel.getRowCount()-1)
				output+="\n";
				
				writer.write(output);
			}
			
			writer.close();

			setWindowTitle(file.getName());
		} catch (Exception ex)
		{
			System.out.println(ex);
			JOptionPane.showMessageDialog(this,"Unable to save file.","Error",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Sets the title of the window adding the current file name at the end or none if null
	 * @param loadedFile currently open file
	 */
	private void setWindowTitle(String loadedFile)
	{
		super.setTitle(loadedFile!=null?"Simple ToDo - "+loadedFile:"Simple ToDo");
	}
	
	/**
	 * Adds a todo task to the table. Used by TaskDialog.
	 * @param taskName name of the new task to add
	 */
	public void eventAddTask(String taskName,Date taskDate)
	{
		todoTableModel.addRow(new Object[]{false,taskName,taskDate});
	}
	
	/**
	 * Edits an existing todo task at the given index. Used by TaskDialog.
	 * @param index row of the task on the todo table
	 * @param taskName name of the new task to add
	 */
	public void eventEditTask(int index,String taskName,Date taskDate)
	{
		todoTable.setValueAt(taskName,index,NAME_COLUMN);
		todoTable.setValueAt(taskDate,index,DATE_COLUMN);
	}
	
	/**
	 * Event fired upon clicking on todo tasks. Updates the state of delete and edit task buttons.
	 */
	private void eventListSelection()
	{
		deleteButton.setEnabled(todoTable.getSelectedRowCount()>0);
		editButton.setEnabled(todoTable.getSelectedRowCount()==1);
	}
	
	/**
	 * Helper method to create toolbar buttons
	 * @param hint tooltip hint for the button
	 * @param icon image file for the button
	 * @return Button returns the generated button
	 */
	private JButton makeToolBarButton(String hint,String icon)
	{
		JButton button=new JButton();
		button.setToolTipText(hint);
		button.addActionListener(this);
		try
		{
			button.setIcon(new ImageIcon(getClass().getResource(icon)));
		} catch (Exception ex)
		{
			System.out.println(ex.getStackTrace());
			button.setText(hint);
		}
		
		return button;
	}

	public static void main(String[] args)
	{
		new ApplicationWindow();
	}
}
