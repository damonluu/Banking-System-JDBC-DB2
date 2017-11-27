/**
 * Main entry to program.
 */
public class ProgramLauncher {

	public static void main(String argv[]) {
		System.out.println(":: PROGRAM START");

		if (argv.length < 1) {
			System.out.println("Need database properties filename");
		} else {
			BankingSystem.init(argv[0]);
			BankingSystem.testConnection();
			BatchInputProcessor.run(argv[0]);
			BankingSystem.promptMainMenu();
			// BankingSystem.gui();  //TO RUN GUI VERSION COMMENT OUT THE LINE ABOVE AND UNCOMMENT THIS LINE
		}
	}
}
