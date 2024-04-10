import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	// This Map stores all the concept names which will later appear in the
	// translpairs and predicates lists of the .dfg file
	public static Map<Integer, String> predicatesConc = new HashMap<>();

	// This Map stores all the role names which will later appear in the translpairs
	// and predicates lists of the .dfg file
	public static Map<Integer, String> predicatesRoles = new HashMap<>();

	// Those variables store the number of entries the both maps contain (this might
	// possibly not be necessary)
	public static int idGenerator1;
	public static int idGenerator2;

	// This a list of keywords in SPASS. In a later function it is used to rename
	// CEL concept names which are coincedently also SPASS keywords
	public static final String[] KEYWORDS = { "date", "description", "test", "hypothesis", "name", "not", "status",
			"sum", "unknown", "version", "range", "equal" };

	public static void main(String[] args) {

		double startTime = System.nanoTime();

		// the global maps and IDs get cleaned up every time you start this programm,
		// just in case
		idGenerator1 = 0;
		idGenerator2 = 0;
		predicatesConc.clear();
		predicatesRoles.clear();

		// here has to fill in the name of the ontology file that is to be translated.
		// also the output file names are generated
		String ontology = "goextendedpseudomodule";
		String celFileName = "./input/" + ontology + ".cel";
		String axiomSetFileName = "./output/" + ontology + ".txt";
		String dfgFileName = "./spassInput/" + ontology.replace("-", "_") + ".dfg";

		// this function call parses the cel file and stores all the lines in a String
		// list
		LinkedList<String> input = loadFile(celFileName);

		// this function call creates the intermediate file in the output folder. it
		// only contains the axiom set resulting from the cel file
		writeFile(input, axiomSetFileName);

		// here the actual .dfg file is created
		try (FileWriter writer = new FileWriter(dfgFileName); BufferedWriter bw = new BufferedWriter(writer)) {

			// File header
			bw.write("begin_problem(" + ontology.replace("-", "_") + ").");
			bw.newLine();
			bw.newLine();
			bw.write("%Headings");
			bw.newLine();
			bw.newLine();
			bw.write("list_of_descriptions.");
			bw.newLine();
			bw.write("name({*Example*}).");
			bw.newLine();
			bw.write("author({*Script by JD*}).");
			bw.newLine();
			bw.write("status(unknown).");
			bw.newLine();
			bw.write("description({* please insert description *}).");
			bw.newLine();
			bw.write("end_of_list.");
			bw.newLine();
			bw.newLine();

			// predicates and translpairs
			bw.write("list_of_symbols.");
			bw.newLine();
			bw.write("%predicates and translpairs");
			bw.newLine();
			bw.newLine();

			bw.write("predicates[");
			bw.newLine();
			bw.write("(" + predicatesConc.get(0) + ",0),(" + predicatesConc.get(0).toUpperCase() + ",1)");
			bw.newLine();
			for (int i = 1; i < idGenerator1; i++) {
				bw.write(",(" + predicatesConc.get(i) + ",0),(" + predicatesConc.get(i).toUpperCase() + ",1)");
				bw.newLine();
			}
			for (int i = 0; i < idGenerator2; i++) {
				bw.write(",(" + predicatesRoles.get(i) + ",0),(" + predicatesRoles.get(i).toUpperCase() + ",2)");
				bw.newLine();
			}
			bw.write("].");
			bw.newLine();
			bw.write("translpairs[");
			bw.newLine();
			bw.write("(" + predicatesConc.get(0) + "," + predicatesConc.get(0).toUpperCase() + ")");
			bw.newLine();
			for (int i = 1; i < idGenerator1; i++) {
				bw.write(",(" + predicatesConc.get(i) + "," + predicatesConc.get(i).toUpperCase() + ")");
				bw.newLine();
			}
			for (int i = 0; i < idGenerator2; i++) {
				bw.write(",(" + predicatesRoles.get(i) + "," + predicatesRoles.get(i).toUpperCase() + ")");
				bw.newLine();
			}
			bw.write("].");
			bw.newLine();

			bw.write("end_of_list.");
			bw.newLine();
			bw.newLine();

			// Ontology
			bw.write("list_of_special_formulae(axioms, DL).");
			bw.newLine();
			bw.newLine();
			bw.write("%Ontology");
			bw.newLine();
			bw.newLine();

			// here the file from the intermediate output folder is inserted into the .dfg
			// file
			File file = new File(axiomSetFileName);

			if (!file.canRead() || !file.isFile())
				System.exit(0);

			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(axiomSetFileName));
				String line = null;
				while ((line = in.readLine()) != null) {
					bw.write(line);
					bw.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}

			bw.newLine();
			bw.write("end_of_list.");
			bw.newLine();
			bw.newLine();

			// Problem
			bw.write("list_of_special_formulae(conjectures,DL). ");
			bw.newLine();
			bw.write("%Problem");
			bw.newLine();
			bw.write("concept_formula( implies(X, Y), C). "); // Please always fill X and Y
			bw.newLine();
			bw.newLine();
			bw.write("end_of_list.");
			bw.newLine();
			bw.newLine();

			// File ending.
			bw.write("list_of_settings(SPASS).");
			bw.newLine();
			bw.write("%File ending");
			bw.newLine();
			bw.newLine();
			bw.write("{*set_flag(DocProof,1).*}");
			bw.newLine();
			bw.newLine();
			bw.write("end_of_list.");
			bw.newLine();
			bw.newLine();
			bw.write("end_problem.");
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		double estimatedTime = System.nanoTime() - startTime;
		System.out.println("This .dfg file was created in " + estimatedTime / Math.pow(10, 9) + " seconds");
	}

	private static LinkedList<String> loadFile(String fileName) {

		File file = new File(fileName);

		if (!file.canRead() || !file.isFile())
			System.exit(0);

		BufferedReader in = null;
		LinkedList<String> data = new LinkedList<>();
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line = null;

			// the index is later used to keep track of the actual index of the axiom which
			// is currently parsed.
			// this is due to the fact that CEL allows line breaks, mid axiom which results
			// in multiple lines actually referring to the same axiom.
			int index = -1;

			while ((line = in.readLine()) != null) {
				line = line.toLowerCase();

				// ;; marks comments in cel files. those lines are discarded
				if (line.startsWith(";;"))
					continue;

				// those are possible beginnings for axioms in CEL. this list of words might not
				// be complete. check the celmanual for further information
				if (line.startsWith("(define") || line.startsWith("(implies") || line.startsWith("(equivalent")
						|| line.startsWith("(role") || line.startsWith("(transitive")
						|| line.startsWith("(reflexive")) {
					data.add(line);
					// the index is increased every time a new axiom start e.g. a line starts with
					// one of the afore mentioned strings.
					index++;
				} else {

					// if a line does not start with one of the afore mentioned strings it is the
					// continuation of the last line of the cel file
					// in this case the concerned axiom gets is expended by this line.
					data.set(index, data.get(index) + line);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
		return data;
	}

	private static void writeFile(LinkedList<String> input, String datName) {

		try (FileWriter writer = new FileWriter(datName); BufferedWriter bw = new BufferedWriter(writer)) {
			int id = 0;

			for (String string : input) {
				// in this part of the code the following things are done:
				// - line breaks, tabs and multiple spaces are removed. this results in the
				// commands being clean and parsable one liners
				// - URL parts of GALEN are removed
				// - URL parts of the NCI ontology are removed
				string = string.replace("\n", " ");
				string = string.replace("\t", " ");
				while (string.contains("  "))
					string = string.replace("  ", " ");
				string = string.replace("|http://www.co-ode.org/ontologies/galen#", "");
				string = string.replaceAll("http://www.mindswap.org/2003/ncioncology.owl#", "");
				string = string.replace("|", "");

				// this function call actually starts translating lines of CEL files into axioms
				// in .dfg format
				String tmp = parse(string);

				// the cel command "define-primitive-role" is not yet parsed correctly. the
				// following if clause is nothing but a
				// hardcoded hotfix which could be added because the command was only used once
				// in my ontologies.
				// TODO implement "define-primitive-role" correctly to get correct results for
				// other uses of this command
				if (tmp.equals("Parsingerror"))
					tmp = "role_formula(implies(comp(part_of,part_of),part_of),";

				// here the axiom ID is added
				tmp = tmp + "A" + id + ").";
				bw.write(tmp);
				bw.newLine();
				id++;
			}

		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}

	}

	static String parse(String input) {
		// this pattern is supposed to capture all basic CEL commands e.g. "(implies",
		// "(define-primitive-concept" etc.
		// this might break as soon as an ontology uses complex concepts on the left
		// side of the command as certain commands allow that.
		// consult the cel manual for further information
		Pattern pattern = Pattern.compile("\\(([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_]+|\\(.+\\)) ([a-zA-Z0-9-_]+|\\(.+\\))\\)");
		// Match the pattern against the input string
		Matcher matcher = pattern.matcher(input);

		// Check if the input string matches the pattern
		if (matcher.matches()) {

			// the command and both arguments it takes
			String commandName = matcher.group(1);
			String arg1 = matcher.group(2);
			String arg2 = matcher.group(3);

			// this function call returns a translated version of the cel command with
			// according arguments
			return command(commandName, arg1, arg2);
		}
		// this pattern is supposed to capture commands which only take one argument
		// like the commands for role properties
		pattern = Pattern.compile("\\(([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_]+)\\)");
		matcher = pattern.matcher(input);

		if (matcher.matches()) {
			String role = matcher.group(2);
			// the role name is saved in case it isn't already part of the mapping
			if (!predicatesRoles.containsValue(role))
				predicatesRoles.put(idGenerator2++, role);

			// .dfg version of axioms for role properties
			// TODO account for all role properties
			if (matcher.group(1).equals("transitive")) {
				return "role_formula(implies(comp(" + role + ", " + role + "), " + role + " ),";
			}
			if (matcher.group(1).equals("reflexive")) {
				return "role_formula(implies(id," + role + " ),";
			}
		}
		// if this is returned there is either something wrong with a already
		// implemented command, or the
		// parsed line uses a not yet implemented CEL command
		return "Parsingerror";
	}

	static String command(String name, String arg1, String arg2) {
		// These variables will store the actual arguments for the parsed command
		String resarg1;
		String resarg2;

		// These flags store the information about resarg1 or resarg2 being primitives
		// concepts or roles. This is important for writing down all the concept and
		// rolen names later.
		boolean prim1 = false;
		boolean prim2 = false;

		// Complex expressions which are build using and, some or comp will be parsed by
		// the expr function. Primitive concepts and roles can be saved directly and
		// marked as primitive
		if (arg1.startsWith("(and") || arg1.startsWith("(some") || arg1.startsWith("(comp")) {
			resarg1 = expr(arg1);

		} else {
			resarg1 = arg1;
			prim1 = true;
		}
		if (arg2.startsWith("(and") || arg2.startsWith("(some") || arg2.startsWith("(comp")) {
			resarg2 = expr(arg2);

		} else {
			resarg2 = arg2;
			prim2 = true;
		}

		// This variable later stores the SPASS command which decides wether the command
		// is a concept_formula or a role_formula. This assignment is a fallback case
		// for faulty parsing.
		String conc = "Fehler";

		// This functions are explained later in detail. In essence they cleanup
		// syntactical collisions with SPASS
		resarg1 = handleKeyword(resarg1).replace("-", "");
		resarg2 = handleKeyword(resarg2).replace("-", "");
		resarg1 = fixnoUpper(resarg1);
		resarg2 = fixnoUpper(resarg2);

		// From here on CEL commands like define-concept are translated to SPASS
		// commands like equiv. Also the concept and role name mappings are filled if
		// necessary.
		if (name.equals("define-concept") || name.equals("equivalent")) {
			name = "equiv";
			conc = "concept_formula";
			if (prim1 && !predicatesConc.containsValue(resarg1))

				predicatesConc.put(idGenerator1++, resarg1);

			if (prim2 && !predicatesConc.containsValue(resarg2))

				predicatesConc.put(idGenerator1++, resarg2);

		}
		if (name.equals("define-primitive-concept") || name.equals("implies")) {
			name = "implies";
			conc = "concept_formula";
			if (prim1 && !predicatesConc.containsValue(resarg1))

				predicatesConc.put(idGenerator1++, resarg1);

			if (prim2 && !predicatesConc.containsValue(resarg2))

				predicatesConc.put(idGenerator1++, resarg2);
		}
		if (name.equals("role-inclusion")) {
			name = "implies";
			conc = "role_formula";
			if (prim1 && !predicatesRoles.containsValue(resarg1))
				predicatesRoles.put(idGenerator2++, resarg1);

			if (prim2 && !predicatesRoles.containsValue(resarg2))
				predicatesRoles.put(idGenerator2++, resarg2);

		}
		if (name.equals("define-primitive-role")) {
			name = "implies";
			conc = "role_formula";
			if (prim1 && !predicatesRoles.containsValue(resarg1))
				predicatesRoles.put(idGenerator2++, resarg1);

			if (prim2 && !predicatesRoles.containsValue(resarg2))
				predicatesRoles.put(idGenerator2++, resarg2);

		}

		return conc + "(" + name + "(" + resarg1 + "," + resarg2 + ")" + ",";
	}

	static String expr(String arg) {
		// Because syntax of and, some and comp are very similar between SPASS and CEL
		// the translation can actually be done by simple string manipulation. But there
		// is used one regex pattern for noticing primitive concept and role names.
		String tmp = arg;
		String res = "";
		Pattern pattern = Pattern.compile("([a-zA-Z0-9-_)]+)");
		Matcher matcher;
		LinkedList<String> stack = new LinkedList<>();
		while (tmp.length() != 0) {
			if (tmp.startsWith("(and")) {
				res = res + "and(";
				tmp = tmp.substring(5);
				stack.add("and");
			} else if (tmp.startsWith("(some")) {
				res = res + "some(";
				tmp = tmp.substring(6);
				stack.add("some1");
			} else if (tmp.startsWith("(comp")) {
				res = res + "comp(";
				tmp = tmp.substring(6);
				stack.add("comp");
			} else if (tmp.startsWith(")")) {
				res = res + ")";
				if (tmp.length() == 1) {
					tmp = "";
				} else
					tmp = tmp.substring(1);
				stack.removeLast();
			} else if (tmp.startsWith(" ")) {
				tmp = tmp.substring(1);
				res = res + ",";
				if (stack.getLast().equals("some1")) {
					stack.removeLast();
					stack.add("some2");
				}
			} else {
				if (tmp.contains(" "))
					matcher = pattern.matcher(tmp.substring(0, tmp.indexOf(" ")));
				else
					matcher = pattern.matcher(tmp);
				if (matcher.matches()) {
					String primarg = matcher.group(1);
					if (primarg.substring(primarg.length() - 1).contains(")")) {
						primarg = primarg.substring(0, primarg.indexOf(")"));
					}
					primarg = handleKeyword(primarg).replace("-", "");
					primarg = fixnoUpper(primarg);
					if (stack.getLast().equals("and") || stack.getLast().equals("some2")) {
						if (!predicatesConc.containsValue(primarg))
							predicatesConc.put(idGenerator1++, primarg);
					} else {
						if (!predicatesRoles.containsValue(primarg))
							predicatesRoles.put(idGenerator2++, primarg);
					}

					res = res + primarg;
					if (matcher.group(1).contains(")"))
						tmp = tmp.substring(matcher.group(1).indexOf(")"));
					else
						tmp = tmp.substring(matcher.group(1).length());
				} else {
					System.out.println(tmp);
					System.err.println("Expressionfehler");
				}
			}

		}
		return res;
	}

	public static boolean isKeyword(String arg) {
		for (String string : KEYWORDS) {
			if (string.equals(arg))
				return true;
		}
		return false;
	}

	public static String handleKeyword(String arg) {
		// replaced letters with other mildly similar letters or numbers to avoid
		// keyword collision with SPASS
		if (isKeyword(arg))
			return arg.replace("e", "3").replace("o", "0").replace("u", "v");
		else
			return arg;
	}

	public static String fixnoUpper(String arg) {
		// the translpairs list and predicates list of a .dfg file currently uses
		// lowercase to uppercase translation.
		// if an argument does not contain any letter to create an uppercase version it
		// appends the letter "a" as a fix.
		if (arg.matches("[0-9_]+")) {
			return arg + "a";
		} else
			return arg;
	}

}
