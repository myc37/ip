package chatbot.util;

import chatbot.datetime.Timestamp;
import chatbot.exception.ChatBotException;

/**
 * Represents an interpreter for user inputs.
 */
public class Parser {

    private final Ui innkeeper;
    private final Storage storage;
    private final TaskList taskList;

    /**
     * Instantiates a new Parser.
     *
     * @param innkeeper The UI.
     * @param storage   The storage.
     * @param taskList  The task list.
     */
    public Parser(Ui innkeeper, Storage storage, TaskList taskList) {
        this.innkeeper = innkeeper;
        this.storage = storage;
        this.taskList = taskList;
    }

    /**
     * Parses the input given by the user and attempts to carry out the appropriate commands.
     *
     * @param rawInput The input given by the user.
     * @return The boolean indicating whether the ChatBot should continue prompting the user for inputs or not.
     */
    public boolean parse(String rawInput) {
        String[] input = rawInput.split(" ");
        String response;
        System.out.println();
        switch (input[0]) {
        case "bye":
            innkeeper.bye();
            return false;
        case "list":
            response = taskList.summary();
            innkeeper.print(response);
            return true;
        case "get":
            try {
                if (input.length > 2) {
                    throw new ChatBotException(
                            "That's too many inputs traveller! You only need to key in the "
                                    + "date or timestamp for which you want to view your tasks!"
                    );
                } else {
                    Timestamp date = new Timestamp(input[1]);
                    response = taskList.getTasksOnDate(date);
                    innkeeper.print(response);
                }
            } catch (ChatBotException e) {
                innkeeper.error(e.getMessage());
            }
            return true;
        case "find":
            response = taskList.getTasksByKeyword(input);
            innkeeper.print(response);
            return true;
        case "mark":
        case "unmark":
            try {
                if (taskList.isEmpty()) {
                    throw new ChatBotException(
                            "Your task list is empty traveller! "
                                    + "Add some tasks first before attempting to mark or unmark!"
                    );
                } else if (input.length > 2) {
                    throw new ChatBotException(
                            "That's too many inputs traveller! "
                                    + "You only need to key in the index of the task you wish to mark or unmark!"
                    );
                } else {
                    response = markOrUnmark(
                            Integer.parseInt(input[1]) - 1,
                            input[0].equals("mark")
                    );
                    innkeeper.chat(response);
                }
            } catch (ChatBotException e) {
                innkeeper.error(e.getMessage());
            } catch (NumberFormatException e) {
                innkeeper.error(
                        "You should mark and unmark tasks using their index rather than title traveller!"
                );
            } catch (ArrayIndexOutOfBoundsException e) {
                innkeeper.error(
                        "You need to key in the index of the task you wish to mark or unmark traveller!"
                );
            }

            return true;
        case "todo":
            try {
                response = taskList.addToDo(input);
                innkeeper.chat(response);
                innkeeper.printNumTasks(taskList.getNumTasks());
                storage.saveChanges(taskList);
            } catch (ChatBotException e) {
                innkeeper.error(e.getMessage());
            }
            return true;
        case "delete":
            try {
                if (taskList.isEmpty()) {
                    throw new ChatBotException(
                            "Your task list is empty traveller! "
                                    + "Add some tasks first before attempting to delete!"
                    );
                } else if (input.length > 2) {
                    throw new ChatBotException(
                            "That's too many inputs traveller! "
                                    + "You only need to key in the index of the task you wish to delete!"
                    );
                } else {
                    response = taskList.delete(
                            Integer.parseInt(input[1]) - 1
                    );
                    innkeeper.chat(response);
                    innkeeper.printNumTasks(taskList.getNumTasks());
                    storage.saveChanges(taskList);
                }
            } catch (ChatBotException e) {
                innkeeper.error(e.getMessage());
            } catch (NumberFormatException e) {
                innkeeper.error(
                        "You should delete tasks using their index rather than title traveller!"
                );
            } catch (ArrayIndexOutOfBoundsException e) {
                innkeeper.error(
                        "You need to key in the index of the task you wish to delete traveller!"
                );
            }
            return true;
        case "guide":
            innkeeper.printGuide();
            return true;
        default:
            String[] temp = rawInput.split("/", 2);
            try {
                if (temp.length == 1) {
                    String[] splitInput = temp[0].split(" ");
                    String type = splitInput[0];
                    if (temp[0].isBlank()) {
                        throw new ChatBotException(
                                "Don't be shy traveller! Type in a command and I will assist you!"
                        );
                    } else if (type.equals("deadline")) {
                        if (splitInput.length == 1) {
                            throw new ChatBotException(
                                "You need to key in the title as well as "
                                        + "due date and time of your deadline traveller!"
                            );
                        } else {
                            throw new ChatBotException(
                                    "You need to include /by in your command to add a deadline traveller!"
                            );
                        }
                    } else if (type.equals("event")) {
                        if (splitInput.length == 1) {
                            throw new ChatBotException(
                                    "You need to key in the title and timestamp of your event traveller!"
                            );
                        } else {
                            throw new ChatBotException(
                                    "You need to include /at in your command to add an event traveller!"
                            );
                        }
                    }
                    throw new ChatBotException();
                } else {
                    response = taskList.add(
                        temp[0].split(" "),
                        temp[1].split(" ")
                    );
                    innkeeper.chat(response);
                    innkeeper.printNumTasks(taskList.getNumTasks());
                    storage.saveChanges(taskList);
                }
            } catch (ChatBotException e) {
                innkeeper.error(e.getMessage());
            }
            System.out.println(taskList.getSet());
            return true;
        }
    }

    /**
     * Either marks or unmarks a task in the task list. Both commands involve similar code, which is why they have been combined into this function.
     *
     * @param index The index of the task in the task list.
     * @param mark  If true, mark the task. Else, unmark the task.
     * @return The response to be outputted via the UI.
     * @throws ChatBotException If the task index is invalid.
     */
    public String markOrUnmark(int index, boolean mark)
            throws ChatBotException {
        if (taskList.isValidIndex(index).equals(true)) {
            String response;
            if (mark) {
                response = taskList.mark(index);
            } else {
                response = taskList.unmark(index);
            }
            storage.saveChanges(taskList);
            return response;
        } else {
            throw new ChatBotException(
                    "This is an invalid task index traveller! You can type list to check all task indexes!"
            );
        }
    }
}
