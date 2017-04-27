import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class FileLoader {

    public String loadText(String name) {
        if(!name.endsWith(".cls")) {
            name += ".cls";
        }
        BufferedReader br = null;
        String resultString = null;
        try {
            // Get the file containing the OpenCL kernel source code
            ClassLoader classLoader = getClass().getClassLoader();
            File clSourceFile = new File(classLoader.getResource(name).getFile());
            // Create a buffered file reader to read the source file
            br = new BufferedReader(new FileReader(clSourceFile));
            // Read the file's source code line by line and store it in a string buffer
            String line = null;
            StringBuilder result = new StringBuilder();
            while((line = br.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            // Convert the string builder into a string containing the source code to return
            resultString = result.toString();
        } catch(NullPointerException npe) {
            // If there is an error finding the file
            System.err.println("Error retrieving OpenCL source file: ");
            npe.printStackTrace();
        } catch(IOException ioe) {
            // If there is an IO error while reading the file
            System.err.println("Error reading OpenCL source file: ");
            ioe.printStackTrace();
        } finally {
            // Finally clean up any open resources
            try {
                br.close();
            } catch (IOException ex) {
                // If there is an error closing the file after we are done with it
                System.err.println("Error closing OpenCL source file");
                ex.printStackTrace();
            }
        }

        // Return the string read from the OpenCL kernel source code file
        return resultString;
    }
}
