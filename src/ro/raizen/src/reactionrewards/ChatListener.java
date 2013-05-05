package ro.raizen.src.reactionrewards;

import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ChatListener implements Listener {
	
	private ReactionRewards plugin;
    
	public ChatListener(ReactionRewards plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		//if the player has the permission to play
		if(e.getPlayer().hasPermission("reactionrewards.play")) {
			//If the question hasn't already been answered
			if(!plugin.getQuestionHandler().isExpired()) {
				//If the answer is correct
				if( isCorrect( e.getMessage() ) ) {
					String rewarded;
					//Handle item and currency rewards differently
					if(!plugin.getQuestionHandler().getReward().equalsIgnoreCase("money")) {
						rewarded = plugin.getQuestionHandler().getReward();
						rewarded = plugin.getQuestionHandler().getRewardAmount() + " " + rewarded; 
					}
					else {
						rewarded = ReactionRewards.getEconomy().format((double) plugin.getQuestionHandler().getRewardAmount());
					}
					
					int elapsedTime = (int) (plugin.getQuestionHandler().getTime()) / 1000;
					//Handle item reward
					if(!plugin.getQuestionHandler().getReward().equalsIgnoreCase("money")) {
						String item = plugin.getQuestionHandler().getRewardId();
						int itemId;
						short damageValue;
						plugin.log.info(item);
						//check if the item id has damage value
						if(item.contains(":")) {
							itemId = Integer.parseInt(item.split(":")[0]);
							damageValue = Short.parseShort(item.split(":")[1]);
							ItemStack reward = new ItemStack(itemId, plugin.getQuestionHandler().getRewardAmount(), damageValue);
						    e.getPlayer().getInventory().addItem(reward);
						    plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"), e.getPlayer().getName(), elapsedTime));
						    e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
						} else {
							itemId = Integer.parseInt(item);
							ItemStack reward = new ItemStack(itemId, plugin.getQuestionHandler().getRewardAmount());
						    e.getPlayer().getInventory().addItem(reward);
						    plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"), e.getPlayer().getName(), elapsedTime));
						    e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
						}
					} else { //handle money reward
						ReactionRewards.getEconomy().depositPlayer(e.getPlayer().getName(), (double) plugin.getQuestionHandler().getRewardAmount());
						plugin.getServer().broadcastMessage(String.format(plugin.getLang("broadcastWin"), e.getPlayer().getName(), elapsedTime));
						e.getPlayer().sendMessage(String.format(plugin.getLang("sendWin"), rewarded));
					}
					
					//Add/update player entry in db with +1 wins
					if(plugin.getDb().isSet(e.getPlayer().getName())) {
						plugin.getDb().updatePlayer(e.getPlayer().getName());
					} else {
						plugin.getDb().insertPlayer(e.getPlayer().getName());
					}
				}
			}
		}
	}
	
	private boolean isCorrect(String a) {
		String[] answers = plugin.getQuestionHandler().getAnswers();
		if(plugin.getQuestionHandler().getType().equalsIgnoreCase("trivia") && containsIgnoreCase(a, answers)) {
			plugin.getQuestionHandler().setExpired();
			return true;
		}
		if(plugin.getQuestionHandler().getType().equalsIgnoreCase("alpha") && !plugin.getCfg("main").getBoolean("alphaCaseSensitive") && containsIgnoreCase(a, plugin.getQuestionHandler().getAnswers())) {
			plugin.getQuestionHandler().setExpired();
			return true;
		}
		else if(!plugin.getQuestionHandler().getType().equalsIgnoreCase("trivia") && Arrays.asList(plugin.getQuestionHandler().getAnswers()).contains(a)) {
			plugin.getQuestionHandler().setExpired();
			return true;
		}
		return false;
	}
	
	private boolean containsIgnoreCase(String str, String[] list){
	    for(String i : list){
	        if(i.equalsIgnoreCase(str))
	            return true;
	    }
	    return false;
	}
	
}
