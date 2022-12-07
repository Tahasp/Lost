package usecases.story;

import java.io.IOException;
import java.util.*;

import entities.events.CombatEvent;
import entities.events.Event;
import entities.events.EventManager;
import entities.items.ItemData;
import entities.items.ItemDataManager;
import entities.player.PlayerData;
import entities.player.PlayerFactory;
import entities.player.PlayerInteractor;
import usecases.combat.CombatInteractor;
import usecases.login.LoginInputBoundary;
import usecases.sound.*;
import usecases.gamesave.*;


public class StorylineInteractor {

    private final StorylineInterface VIEW;
    private final SoundInteractor SOUND;
    private final Saver SAVE;
    private final Loader LOAD;
    private final PlayerInteractor player_action;
    private final CombatInteractor COMBAT;
    private final EventManager MANAGER;
    private final LoginInputBoundary LOGIN;

    private final ItemDataManager ITEMDATA;

    public StorylineInteractor(StorylineInterface story, SoundInteractor soundInteractor,
                               Saver saveInteractor, Loader loadInteractor,
                               PlayerInteractor playerInteractor, CombatInteractor combatInteractor,
                               EventManager eventManager, LoginInputBoundary loginInteractor) {
        VIEW = story;
        SOUND = soundInteractor;
        LOAD = loadInteractor;
        SAVE = saveInteractor;
        player_action = playerInteractor;
        COMBAT = combatInteractor;
        MANAGER = eventManager;
        LOGIN = loginInteractor;
        ITEMDATA = new ItemDataManager();
    }

    /** Set Player on their first Event. The method
     * takes in the username set by LoginInteractor.
     */
    public void startGame() throws IOException {
        //create a file to save this user
        saveGame();

        //Setting up a player
        String username = LOGIN.getCurrentUser();
        HashMap<String, ArrayList<ItemData>> inventory = new HashMap<>(); //empty Hash Map
        player_action.updatePlayer((PlayerFactory.generatePlayer(username,
                0, 100, inventory)));
        ArrayList<ItemData> items = (ArrayList<ItemData>) ITEMDATA.getItemData();
        for (ItemData item : items) {
            player_action.addToInventory(item);
        }

        // stop sounds before the first event begins
        SOUND.stopSound();
        SOUND.switchSoundChoice();

        //play the first event
        playEvent();
    }

    /** update the Player's current event based on the choice Player makes that correspond
     * to the UUID from ChoicesNextUUIDs in Event
    * @param choice the integer choice the Player makes
    */
    public void updateEventID(int choice) {
        Map<Integer, Event> event_map = MANAGER.getAllEvents();
        // System.out.println(player_action);
        Event event = event_map.get(player_action.getPlayerEventID());
        if (event.getChoicesNextUUIDs().get(choice) >= 0) {
            player_action.updateEvent(event.getChoicesNextUUIDs().get(choice));
        }else{
            endGame();
        }

    }

    /** Loads the current event of the Player
     */
    public void loadGame() throws IOException, ClassNotFoundException {
        String username = LOGIN.getCurrentUser() + ".ser";

        //load method
        player_action.updatePlayer(LOAD.readFromFile("data/savefiles/" + username)); // loading a player

        //start the game on the current event
        System.out.println(player_action.getPlayerEventID());
        this.playEvent();
    }

    /** Saves PLayer data to the file
     */
    public void saveGame() throws IOException {
        String filename = "data/savefiles/" + LOGIN.getCurrentUser() + ".ser";

        SAVE.saveToFile(filename, player_action.getReference());
        System.out.println("saved!");
    }

    /** Returns the Player back to the homescreen
     */
    public void endGame() {
        VIEW.returnHomeScreen();
    }

    /** If the Player loses the game, bring the Player back to the last save
     * The player
     */
    public void lose() {
        SOUND.stopSound();
        VIEW.displayLose();
        VIEW.display_exit_options();
    }

    /** Exit the game
     * @param player The Player
     */
    @SuppressWarnings("all")  // function is reserved for debugging
    public void exitGame(PlayerData player) {
        VIEW.display_exit_options();
    }

    /**Turn sound on or off
     */
    public void soundSwitch() {
        SOUND.switchSoundChoice();
        if (SOUND.getSoundChoice()) {
            SOUND.playSound();
        }
        else {
            SOUND.stopSound();
        }
    }

    /**Play Homescreen sound*/
    public void homeSoundPlay() {
        String HOME_SOUND_FILE = "data/sound/morning-funny-beat.wav";
        SOUND.createSound(HOME_SOUND_FILE);
        SOUND.playSound();
    }

    /**Stop Homescreen sound*/
    public void homeSoundStop() {
        SOUND.stopSound();
    }

    /** Grab an event based on the inputted UUID and output its sound file and narration.
     * At the end of the narration, let the Player make a choice based on the Event's
     * choicesNarration. If Event is a CombatEvent, make the Player fight the Enemy after
     * the narration. The method should also Save the game if the Event has an Auto Save
     */
    public void playEvent() throws IOException {
        Map<Integer, Event> event_map = MANAGER.getAllEvents();
        Event event = event_map.get(player_action.getPlayerEventID());

        //finish the game. final event id is 1000
        if (player_action.getPlayerEventID() == 1000) {
            this.saveGame();
            this.endGame();
        }

        //load the sound file
        SOUND.closeSound();
        SOUND.createSound(event.getSoundFile());

        // playing or not playing sound
        if (SOUND.getSoundChoice()) {
            SOUND.stopSound();
            SOUND.playSound();
        }

        if (event.getDoesAutoSave()) {
            this.saveGame();
        }

        if (event instanceof CombatEvent) {
            VIEW.displayNarration(event.getNarration());

            if (COMBAT.combat(event.getUUID())) {
                player_action.updateEvent(event.getChoicesNextUUIDs().get(1));
                VIEW.printWin();
                playEvent();
            }
            else {
                //Player loses
                this.lose();
            }

        }
        else {
            VIEW.displayNarration(event.getNarration());
            VIEW.askQuestion("", event.getChoicesNarrations(), null, false);
        }
    }
}
