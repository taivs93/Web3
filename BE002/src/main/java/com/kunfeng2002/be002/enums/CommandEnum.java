package com.kunfeng2002.be002.enums;

import java.util.Arrays;
import java.util.Optional;

public enum CommandEnum {
    START("/start", "Show welcome message"),
    HELP("/help", "Show help message"),
    FOLLOW("/follow", "Follow a wallet address"),
    UNFOLLOW("/unfollow", "Unfollow a wallet address"),
    LIST("/list", "List followed addresses"),
    SEARCH("/search", "Search for coins"),
    GAS("/gas", "Show gas fee estimate"),
    SET_ALERT("/setalert", "Set price alert"),
    LIST_ALERTS("/listalerts", "List your alerts"),
    DEL_ALERT("/delalert", "Delete alert"),
    LINK("/link", "Link telegram to web account"),
    PORTFOLIO("/portfolio", "Create new portfolio"),
    ADD_TOKEN("/addtoken", "Add token to portfolio"),
    MY_PORTFOLIOS("/myportfolios", "List your portfolios"),
    NEW_COINS("/newcoins", "Show newest coins"),
    DCA("/dca", "Create DCA plan"),
    MY_DCA("/mydca", "List your DCA plans"),
    STOP_DCA("/stopdca", "Stop DCA plan"),
    TEST_DCA("/testdca", "Test DCA notifications");

    private final String command;
    private final String description;

    CommandEnum(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<CommandEnum> fromCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedCommand = command.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(cmd -> cmd.command.equals(normalizedCommand))
                .findFirst();
    }

    public static boolean isValidCommand(String command) {
        return fromCommand(command).isPresent();
    }

    public static String getHelpText() {
        StringBuilder help = new StringBuilder();
        help.append("Welcome to Web3 Chat Bot!\n\n");
        help.append("Commands:\n");
        
        help.append("Basic Commands:\n");
        help.append(START.command).append(" - ").append(START.description).append("\n");
        help.append(HELP.command).append(" - ").append(HELP.description).append("\n");
        help.append(FOLLOW.command).append(" <wallet_address> - ").append(FOLLOW.description).append("\n");
        help.append(UNFOLLOW.command).append(" <wallet_address> - ").append(UNFOLLOW.description).append("\n");
        help.append(LIST.command).append(" - ").append(LIST.description).append("\n");
        help.append(SEARCH.command).append(" <query> - ").append(SEARCH.description).append("\n");
        help.append(GAS.command).append(" <network> [gasLimit] - ").append(GAS.description).append("\n");
        
        help.append("\nAlert Commands:\n");
        help.append(SET_ALERT.command).append(" <symbol> <price> - ").append(SET_ALERT.description).append("\n");
        help.append(LIST_ALERTS.command).append(" - ").append(LIST_ALERTS.description).append("\n");
        help.append(DEL_ALERT.command).append(" <price> - ").append(DEL_ALERT.description).append("\n");
        
        help.append("\nAccount Commands:\n");
        help.append(LINK.command).append(" <linking code> - ").append(LINK.description).append("\n");
        
        help.append("\nPortfolio Commands:\n");
        help.append(PORTFOLIO.command).append(" <name> - ").append(PORTFOLIO.description).append("\n");
        help.append(ADD_TOKEN.command).append(" <symbol> <amount> <price> - ").append(ADD_TOKEN.description).append("\n");
        help.append(MY_PORTFOLIOS.command).append(" - ").append(MY_PORTFOLIOS.description).append("\n");
        help.append(NEW_COINS.command).append(" - ").append(NEW_COINS.description).append("\n");
        
        help.append("\nDCA Commands:\n");
        help.append(DCA.command).append(" <symbol> <amount> <WEEKLY|MONTHLY|QUARTERLY> <HH:MM> - ").append(DCA.description).append("\n");
        help.append(MY_DCA.command).append(" - ").append(MY_DCA.description).append("\n");
        help.append(STOP_DCA.command).append(" <id> - ").append(STOP_DCA.description).append("\n");
        help.append(TEST_DCA.command).append(" - ").append(TEST_DCA.description).append("\n");
        
        return help.toString();
    }
}
