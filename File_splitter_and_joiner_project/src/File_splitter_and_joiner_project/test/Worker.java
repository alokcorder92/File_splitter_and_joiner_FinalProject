package File_splitter_and_joiner_project.test;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;

public class Worker extends SwingWorker{

	private Splitter splitter;
	private File inputFile, targetFolder;
	private int fragmentSize;
	private BufferedInputStream fis;
	private int start, end, completed;
	private int fragmentCount=0;
	private ArrayList<File> fragments;
	private ArrayList<String> tempFiles;

	private long netTime;

	private static final short COMMAND_LIMIT=15;

	public Worker(Splitter s){

		splitter=s;
		fragments = new ArrayList<File>();
		tempFiles = new ArrayList<String>();

	}

	private void split() throws FileNotFoundException, IOException, InterruptedException, InvalidInputException {

		fis=new BufferedInputStream(new FileInputStream(inputFile));
		start=0;
		end=(int)inputFile.length();

		if(end < 0){
			String m="Source file is too large.";
				   m+="\nThis version of FileSplitter could only handle files of size 4.3GB or less.";
			throw new InvalidInputException(m);
		}

		completed=0;
		fis.mark(completed);
		int numFragments = (int)Math.ceil(end/fragmentSize) + 1;
		int loopCount=0;
		splitter.prepareMonitor(numFragments);

		long startTime=System.currentTimeMillis();
		while(completed < end){

			byte[] bytes=readFragment();
			splitter.updateMonitor(++loopCount);

			fragmentCount++;

			if(Thread.interrupted()){
				throw new InterruptedException("Interrupted by user.");
			}
			
			writeFragment(bytes);
			splitter.updateMonitor(++loopCount);

		}
		long endTime=System.currentTimeMillis();
		netTime=endTime-startTime;

		createBatchFile();

	}//split

	private byte[] readFragment() throws IOException, InterruptedException {

		int bytesToRead=fragmentSize;
		int bytesRemaining=end-completed;

		if(bytesRemaining < fragmentSize ){
			bytesToRead=end-completed;
		}

		byte[] bytes=new byte[bytesToRead];

		if(Thread.interrupted()){
			throw new InterruptedException("Interrupted by user.");
		}

		int bytesRead=fis.read(bytes);

		completed += bytesRead;
		fis.mark(completed);

		return bytes;

	}

	private void writeFragment(byte[] bytes) throws FileNotFoundException, IOException, InterruptedException {
	
		BufferedOutputStream fos=null;
		
		try{
		
			String prefix="";
			if(fragmentCount <= 9){
				prefix="0";
			}
			File target=new File(targetFolder.getPath()+System.getProperty("file.separator")+inputFile.getName()+".xs"+prefix+fragmentCount);
			fos=new BufferedOutputStream(new FileOutputStream(target));
	
			if(Thread.interrupted()){
				throw new InterruptedException("Interrupted by user.");
			}

			fos.write(bytes);
			fragments.add(target);
			
		}
		catch(FileNotFoundException fnfe){
			System.out.println("Writing...");
			String m="There was an error while writing fragments to disk.";
				   m+="\n" + fnfe.getMessage()+".";

			throw new FileNotFoundException(m);

		}
		catch(IOException ioe){

			String m="There was an error while writing fragments to disk.";
				   m+="\n" + ioe.getMessage()+".";

			throw new IOException(m);

		}
		finally{
			if(fos != null){
				fos.close();
			}
		}

	}

	private String generateCommand(int st, int count, int id){

		int start=st;
		String command="copy /b ";

		for(int i=0; i < count; i++){

			if( i == (count-1)){
				command += "\"*.xs"+ start + "\"" + " /b ";
			}
			else{
				String prefix="";
				if(start <= 9){
					prefix="0";
				}
				command += "\"*.xs" + prefix + start  +"\" /b + ";
			}
			start++;
		}

		String tempFile=inputFile.getName() + "_TMP_"+id;

		command += "\""+tempFile+"\"";
		tempFiles.add(tempFile);

		return command;

	}

	private void createBatchFile(){

		int id=0;

		String command="";

		for(int i=1; i<= fragmentCount; i+= COMMAND_LIMIT){

			command+="\n"+generateCommand(i, COMMAND_LIMIT, ++id);

		}//for

		String subCommand=generateTempCommand();
		command +=subCommand;

		writeBatchFile(command);

	}

	private String generateTempCommand(){

		String subCommand="\n\ncopy /b ";
		for(int i=0; i<tempFiles.size(); i++){

			String tmpFile=(String)tempFiles.get(i);

			if( i ==  (tempFiles.size()-1) ){
				subCommand += "\""+tmpFile+"\""+"/b ";
			}
			else{
				subCommand += "\""+tmpFile+"\""+"/b + ";
			}
		}

		subCommand += "\""+inputFile.getName()+"\"";

		subCommand+="\n\n";
		for(int i=0; i<tempFiles.size(); i++){
			subCommand+="\ndel " + "\""+(String)tempFiles.get(i)+"\"";
		}

		return subCommand;

	}

	private void writeBatchFile(String command){

		FileOutputStream fos=null;

		try{
			String path=inputFile.getParent();
			fos=new FileOutputStream( new File(targetFolder.getPath() + System.getProperty("file.separator") + "Rejoin.bat") );
			fos.write(command.getBytes());
			fos.flush();

		}
		catch(IOException ioe){

			String m="There was an error while writing batch file.";
				   m+="\n"+ioe.getMessage()+".";
			JOptionPane.showMessageDialog(splitter, m, "FileSplitter", JOptionPane.ERROR_MESSAGE);

		}
		finally{
			if(fos != null){
				try{
					fos.close();
				}
				catch(IOException x){
					//Ignore
				}
			}
		}

	}

	private int getUserOption(String folder){

			String title="FileSplitter";
			String message="The folder you specified for fragments does not exist.";
			         message+="\nWould you like to create '"+System.getProperty("user.dir")+System.getProperty("file.separator")+folder+"' ?";

			Object[] options={"Yes", "No"};
			Object initialValue=options[0];

			int option=JOptionPane.showOptionDialog(splitter, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialValue);
			return option;

	}

	public Object construct(){

		try{

			File inputFile=splitter.getSourceFile();
			if(!inputFile.exists()){

				String m="The source file you specified either does not exist or invalid.";
					   m+="\nClick \'Browse\' to select a source file.";

				throw new InvalidInputException(m);

			}

			targetFolder=splitter.getTargetFile();
			if(!targetFolder.exists()){

				int option=getUserOption(targetFolder.toString());
				if(option == JOptionPane.YES_OPTION){
					targetFolder.mkdir();
				}else{
					String m="FileSplitter will not continue.";
						 m+="\nYou must specify a valid folder for fragments.";
					throw new InvalidInputException(m);
				}
			}

			int fragmentSize=splitter.getFragmentSize();
			if(fragmentSize > inputFile.length()){

				String m="Fragment size cannot exceed source file size.";
					   m+="\nPlease specify a valid value and try again.";

				throw new InvalidFragmentSizeException(m);

			}

			this.inputFile=inputFile;
			this.fragmentSize=fragmentSize;

			split();
			finish();

		}
		catch(InterruptedException ie){

			removeOrphans();
			splitter.resetMonitor();

		}
		catch(InvalidFragmentSizeException ifse){

			JOptionPane.showMessageDialog(splitter, ifse.getMessage(), "FileSplitter", JOptionPane.ERROR_MESSAGE);

		}
		catch(InvalidInputException iie){

			JOptionPane.showMessageDialog(splitter, iie.getMessage(), "FileSplitter", JOptionPane.ERROR_MESSAGE);

		}
		catch(NoFileSpecifiedException nfse){

			JOptionPane.showMessageDialog(splitter, nfse.getMessage(), "FileSplitter", JOptionPane.ERROR_MESSAGE);

		}
		catch(FileNotFoundException fnfe){

			JOptionPane.showMessageDialog(splitter, fnfe.getMessage(), "FileSplitter - Not Found", JOptionPane.ERROR_MESSAGE);
			splitter.resetMonitor();

		}
		catch(IOException ioe){

			JOptionPane.showMessageDialog(splitter, ioe.getMessage(), "FileSplitter - IO Error", JOptionPane.ERROR_MESSAGE);
			splitter.resetMonitor();

		}
		catch(Exception e){

			String m="An unknown error stopped FileSplitter from servicing your request.";
				  m+="\nError Message: "+e.getMessage();
			JOptionPane.showMessageDialog(splitter, m, "FileSplitter - Unknown Error", JOptionPane.ERROR_MESSAGE);

		}
		finally{

			if(fis != null){
				try{
					fis.close();
				}catch(IOException x){
					//Ignore
				}
			}
			splitter.activateControls();
			System.gc();

		}
		return "Completed";

	}

	private void removeOrphans(){

		int size=fragments.size();

		if(size > 0){

			String message=size+" fragment was orphaned. What would you like to do with the fragment ?";
			if(size > 1){
				message=size+ " fragments were orphaned. What would you like to do with these fragments ?";
			}

			String title="FileSplitter - Stopped!";
			Object[] options={"Keep", "Remove"};
			Object initialValue=options[0];

			int option=JOptionPane.showOptionDialog(splitter, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, initialValue);

			if(option == JOptionPane.NO_OPTION){

				for(int i=0; i<size; i++){
						((File)fragments.get(i)).delete();
				}

			}

		}
	}

	private void finish(){

		String m=fragmentCount + " fragments written to "+targetFolder.getAbsolutePath()+".";
			  m+="\nRun Rejoin.bat to splice the fragments.";

		DecimalFormat df=new DecimalFormat("#.##");
		double d=(double)netTime/1000;
		String title="FileSplitter - Done [Time Elapsed: "+df.format(d)+" sec]";

		JOptionPane.showMessageDialog(splitter, m, title, JOptionPane.INFORMATION_MESSAGE);

		splitter.reset();

	}
}
