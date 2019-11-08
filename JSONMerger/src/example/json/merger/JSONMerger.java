/**
 * 
 */
package example.json.merger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Prasad.Chillapalli
 *
 */
public class JSONMerger {

	/**
	 * @param args arg1 = folder path arg2 = Input file base name arg3 = Output file
	 *             base name arg4 = Max File Size ( in bytes )
	 * @throws ParseException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ParseException {
		String folder_path = null;
		String[] in_base_name = new String[1];
		in_base_name[0] = null;
		String out_base_name = null;
		String max_size = null;
		try {
			folder_path = args[0];
			in_base_name[0] = args[1];
			out_base_name = args[2];
			max_size = args[3];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No command line arguments given");
			System.out.println(
					"Enter <absolute folder path><space><input file base name><space><output file base name><space><output file base name><space><max file size (in bytes)>");
			System.out.println("Demo input => ./src/example/json/datafiles data merge 1250");

			Scanner scan = new Scanner(System.in);
			String input = scan.nextLine();
			if (input.isEmpty()) {
				String demo_input = "./src/example/json/datafiles data merge 1250" + "";
				System.out.println("No input => Taking Demo input : " + demo_input);
				input = demo_input;
			}
			args = input.split("\\s");
			folder_path = args[0];
			in_base_name[0] = args[1];
			out_base_name = args[2];
			max_size = args[3];
			scan.close();
		}
		FileFilter jsonFilefilter = new FileFilter() {
			// Override accept method
			public boolean accept(File file) {
				// if the file extension is of in_base_name and .json return true, else false
				if (file.getName().endsWith(".json") && file.getName().startsWith(in_base_name[0])) {
					return true;
				}
				return false;
			}
		};
		File filein = new File(folder_path);
		int counter = 0;
		JSONParser parser = new JSONParser();
		if (filein.isDirectory()) {
			boolean write = true;
			JSONObject[] prevobj = new JSONObject[1];
			for (File files : filein.listFiles(jsonFilefilter)) {
//				Listing out all files in order
				System.out.println("Input file name: " + files.getName());
				FileReader fin = new FileReader(files);
//				Reading current file as JSON Object
				JSONObject objin = (JSONObject) parser.parse(fin);
				if (prevobj[0] != null) {
//					If a file exists for merging
					JSONObject[] finalobj = new JSONObject[1];
					finalobj[0] = new JSONObject();
					prevobj[0].keySet().forEach(key -> {
						if (objin.containsKey(key)) {
							JSONArray jsonarray = (JSONArray) prevobj[0].get(key);
							JSONArray jsonarray2 = (JSONArray) objin.get(key);
							for (int i = 0; i < jsonarray2.size(); i++) {
								jsonarray.add(jsonarray2.get(i));
							}
							finalobj[0].put(key, jsonarray);
						}
					});
					prevobj[0] = finalobj[0];
				} else {
//					If this is the first file/single file no merge happens
					prevobj[0] = objin;
					counter++;
					continue;

				}

				File fileout = new File(folder_path + "/" + out_base_name + counter + ".json");
				FileWriter fout = new FileWriter(fileout);

				if (counter > 0 && write) {
					try {
//						Writing the merged json to file (out_base_name + counter + .json)
						org.json.JSONObject tmp = new org.json.JSONObject(prevobj[0].toString());
						fout.write(tmp.toString(4));
					} catch (Exception e) {
						e.printStackTrace();
						fout.write(prevobj[0].toJSONString());
					}
					fout.flush();
					fout.close();
					if(counter - 1 > 0) {
						new File(folder_path + "/" + out_base_name + (counter-1) + ".json").delete();
					}

					if (fileout.length() > Long.parseLong(max_size)) {
//						Checking if the file exceeds the current specified limit (max_size)
						write = false;
						fileout.delete();
						break;
					}

					System.out.println("Output file name: " + fileout.getName());
					System.out.println("File size: " + fileout.length());
				}

				counter++;
			}

		} else {
			System.out.println("Enter directory name");
		}
	}

}
