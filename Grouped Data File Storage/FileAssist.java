package oregontrailpackage;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Contains methods that are used for dealing with information in files.
 * Originally written for a webcomic library project and slightly altered for this project.
 * Warning: this class is VERY inefficient, it was created for ease of coding.
 * @author Bo Lenhardt
 * @version 2.0
 */
public class FileAssist
{
    private static final String FLAG = "***";
    private static final int READ = 0;
    private static final int WRITE = 1;

    private String path;
    private String name;
    private String fullpath;
    private File file;
    private BufferedReader in;
    private BufferedWriter out;
    
    /**
     * Attempts to create a new FileAssist from the given filename using the
     * directory specified in Comic_Checker.PATH. Will create a new, blank, file
     * if none is found.
     *
     * @param name name of the file to use, not including path or extension
     */
    public FileAssist(String name)
    {
        this.name = name;

        path = System.getProperty("user.dir") + "/files/";
        fullpath = path + name + ".txt";
        
        file = new File(fullpath);
    }
    
    /**
     * Returns whether the file this represents exists. 
     * 
     * @return whether the file this represents exists.
     * @see createFile()
     * @Warning failure to ensure file existence may result in errors
     */
    public boolean exists()
    {
        return file.exists();
    }

    /**
     * Creates the file this FileAssist represents.
     * You can add sections and then data after calling this method.
     * Will not overwrite existing files.
     * 
     * @see exists()
     * @Warning attempts to read a blank file will yield unpredictable results.
     */
    public void createFile()
    {
        try
        {
            if (!file.exists())
            {
                File dir = new File(path);
                if(!dir.exists())
                {
                    dir.mkdirs();
                }
                file.createNewFile();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(FileAssist.class.getName()).log(Level.SEVERE, name + " not found.", ex);
        }
    }
    
    private void setOperation(int operation)
    {
        try
        {
            switch (operation)
            {
                case READ:
                    in = new BufferedReader(new FileReader(fullpath));
                    return;
                case WRITE:
                    out = new BufferedWriter(new FileWriter(fullpath));
                    return;
            }
        } 
        catch (IOException e)
        {
            Logger.getLogger(FileAssist.class.getName()).log(Level.SEVERE, "error setting operation to " + operation, e.getStackTrace());
        }
    }

    /**
     * Returns the filename that created this FileAssist instance.
     *
     * @return the filename that created this FileAssist instance.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Goes through lines until either the section has been found or the
     * document has no lines left. once it finds it, a new loop starts that ends
     * with either the termination of the section or the document. It records
     * all lines found in the section and returns them in the form of a String
     * ArrayList.
     *
     * @param start - The name of the section searched
     * @return An ArrayList containing all of the lines found in the section.
     * Will return an empty ArrayList if either the section does not exist or if
     * nothing is found in it.
     */
    public ArrayList<String> getLinesIn(String start)
    {
        setOperation(READ);
        
        ArrayList<String> returner = new ArrayList<>();

        loop:
        while (true)
        {
            String tester = "";

            try
            {
                tester = in.readLine();
                if(tester == null)break;
            } catch (IOException outOfLines)
            {
                break;
            }

            //intended section has been found
            if (tester.equals(FLAG + start + FLAG))
            {
                while (true)
                {
                    try
                    {
                        String value = in.readLine();
                        //a different section has started (end search)
                        if (value == null || value.startsWith(FLAG))
                        {
                            break loop;
                        }

                        //add line to list (default)
                        returner.add(value);
                    } catch (IOException outOfLines)
                    {
                        break loop;
                    }//catch
                }//while
            }//if
        }//while
        try
        {
            in.close();
        }
        catch (IOException e){}
        
        return returner;
    }//findLinesIn

    /**
     * Similar to getLinesIn(String, DataInputStream). Some types of information
     * would generally only have one string value for it, so this method returns
     * the String value of just the first line found in the section.
     *
     * @param start - The name of the category searched
     * @return the first line found in the section
     * @Warning if there is no data held in the section this returns null.
     */
    public String getLineIn(String start)
    {
        ArrayList<String> lines = getLinesIn(start);
        if(lines.isEmpty())
            return "";
        return lines.get(0);
    }

    /**
     * Changes the text in the text document within a section to the given
     * lines.
     *
     * @param start - The section to edit
     * @param data - All lines intended to be placed in the section
     *
     * @Warning Any data previously held in the section will be lost.
     */
    public void setLinesIn(String start, ArrayList<String> data)
    {
        ArrayList<ArrayList<String>> comicData = new ArrayList<ArrayList<String>>();

        setOperation(READ);

        String sectionName;
        try
        {
            sectionName = in.readLine();
        } catch (IOException noLines)
        {
            return;
        }
        //record all data in a series of ArrayLists, each iteration represents one section
        section:
        while (true)
        {
            //test for found section; if found, skip normal procedure
            if (sectionName.equals(FLAG + start + FLAG))
            {
                ArrayList<String> sectionData = new ArrayList<String>();
                sectionData.add(sectionName);
                sectionData.addAll(data);
                comicData.add(sectionData);

                //get to the next section while skipping over data in this one
                while (true)
                {
                    String line;
                    try
                    {
                        line = in.readLine();
                        if(line == null)
                        {
                            break section;
                        }
                    } catch (IOException outOfLines)
                    {
                        break section;
                    }

                    if (line.startsWith(FLAG))
                    {
                        sectionName = line;//next section starts with last line found
                        continue section;
                    }
                }
            }

            //normal section procedure
            ArrayList<String> sectionData = new ArrayList<String>();
            sectionData.add(sectionName);
            while (true)//each line in the section
            {
                String line;
                try
                {
                    line = in.readLine();
                    if(line == null)break section;
                } catch (IOException outOfLines)
                {
                    break section;
                }

                if (line.startsWith(FLAG))
                {
                    comicData.add(sectionData);
                    sectionName = line;//next section starts with last line found
                    continue section;
                } else
                {
                    sectionData.add(line);
                }//else
            }//while lines in section
        }//while section
        try
        {
            in.close();
        }
        catch (IOException ex){}
        //save all data
        setOperation(WRITE);
        try
        {
            for (ArrayList<String> section : comicData)
            {
                for (String line : section)
                {
                    out.write(line);
                    out.newLine();
                }
            }
            out.close();
        }
        catch (IOException outputfailed)
        {
            JOptionPane.showMessageDialog(null, "Attempt to save data failed.");
        }
    }

    /**
     * Simplifies setting data in fields that would reasonably only have one
     * line. Calls setLinesIn with a created ArrayList of one item
     *
     * @param start - The section to be edited
     * @param data - The information to fill the section
     */
    public void setLineIn(String start, String data)
    {
        ArrayList<String> line = new ArrayList<String>();
        line.add(data);

        setLinesIn(start, line);
    }

    public ArrayList<String> getAllLines()
    {
        ArrayList<String> returner = new ArrayList<String>();

        setOperation(READ);

        while (true)
        {
            try
            {
                String line = in.readLine();
                if(line == null)break;
                returner.add(line);
            }
            catch (IOException outOfLines)
            {
                break;
            }
        }
        
        try
        {
            in.close();
        }
        catch (IOException ex){}
        
        return returner;
    }

    public void addSection(String name)
    {
        ArrayList<String> data;
        try
        {
            setOperation(READ);
            data = getAllLines();
            data.add(FLAG + name + FLAG);
            in.close();
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "Difficulty preparing to add section");
            return;
        }

        try
        {
            setOperation(WRITE);
            for (int i = 0; i < data.size(); i++)
            {
                out.write(data.get(i));
                out.newLine();
            }
            out.close();
        } catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null, "Difficulty saving section");
        }
    }
    
    /**
     * Returns all sections in the file.
     * @return all sections in the file
     */
    public ArrayList<String> getSections()
    {
        setOperation(READ);
        ArrayList<String> sections = new ArrayList<String>();
        while(true)
        {
            try
            {
                String line = in.readLine();
                if(line == null)break;
                if(line.startsWith(FLAG))
                    sections.add(line.substring(FLAG.length(), line.length() - FLAG.length()));
            }
            catch(IOException e)
            {
                break;
            }
        }
        
        try
        {
            in.close();
        }
        catch (IOException ex){}
        
        return sections;
    }
}//class
