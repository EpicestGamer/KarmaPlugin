/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.epicest.spigot.karma;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.*;

/**
 *
 * @author mjspr
 */
public class Karma2 extends JavaPlugin {

 public static final Logger LOG = Logger.getLogger("Minecraft");

 public static final String KARMA = "karma";

 public static final String KARMA_DEPOSIT = "deposit";
 public static final String KARMA_DEPOSIT_LIST = "list";

 public static final String KARMA_BALANCE = "balance";

 public static final String KARMA_WITHDRAW = "withdraw";
 public static final String KARMA_WITHDRAW_LIST = "list";
 public static final String KARMA_WITHDRAW_PRIZE = "prize";
 public static final String KARMA_WITHDRAW_DONATION = "donation";

 public static final ChatColor primary = ChatColor.GOLD;
 public static final ChatColor secondary = ChatColor.BLUE;

 protected Properties karmapoints = new Properties();
 protected Properties karmadonations = new Properties();

 public void onEnable() {
  LOG.info("[Karma] Checking for config.yml");
  if (!getDataFolder().exists()) {
   getDataFolder().mkdirs();
  }
  File config = new File(getDataFolder(), "config.yml");
  if (!config.exists()) {
   LOG.info("[Karma] Check failed, copying default config");
   saveDefaultConfig();
  } else {
   LOG.info("[Karma] Check succeeded");
  }
  LOG.info("[Karma] Loading data");
  File karmapointsFile = new File(getDataFolder(), "karmapoints.properties");
  try {
   if (karmapointsFile.exists()) {
    FileInputStream fin = new FileInputStream(karmapointsFile);
    BufferedInputStream bin = new BufferedInputStream(fin);
    karmapoints.load(bin);
    LOG.info("[Karma] Karma Points loaded");
   } else {
    karmapointsFile.createNewFile();
    LOG.info("[Karma] Karma Points file does not exist, creating new file");
   }
  } catch (IOException ex) {
   LOG.log(Level.SEVERE, null, ex);
  }
  File karmadonationsFile = new File(getDataFolder(), "karmadonations.properties");
  try {
   if (karmadonationsFile.exists()) {
    FileInputStream fin = new FileInputStream(karmadonationsFile);
    BufferedInputStream bin = new BufferedInputStream(fin);
    karmadonations.load(bin);
    LOG.info("[Karma] Karma Donations loaded");
   } else {
    karmadonationsFile.createNewFile();
    LOG.info("[Karma] Karma Donations file does not exist, creating new file");
   }
  } catch (IOException ex) {
   LOG.log(Level.SEVERE, null, ex);
  }
 }

 public void onDisable() {
  LOG.info("[Karma] Saving data");
  File karmapointsFile = new File(getDataFolder(), "karmapoints.properties");
  try {
   FileOutputStream fout = new FileOutputStream(karmapointsFile);
   karmapoints.store(fout, "Karmapoints Data File");
   LOG.info("[Karma] Karma Points file saved");

  } catch (IOException ex) {
   LOG.log(Level.SEVERE, null, ex);
  }
  File karmadonationsFile = new File(getDataFolder(), "karmadonations.properties");
  try {
   FileOutputStream fout = new FileOutputStream(karmadonationsFile);
   karmadonations.store(fout, "Karmadonations Data File");
   LOG.info("[Karma] Karma Donations file saved");
  } catch (IOException ex) {
   LOG.log(Level.SEVERE, null, ex);
  }
 }

 public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
  switch (command.getName()) {
   case KARMA:
    if (arguments.length > 0) {
     switch (arguments[0]) {
      case KARMA_BALANCE:
       return balance(sender, command, label, arguments);
      case KARMA_DEPOSIT:
       return deposit(sender, command, label, arguments);
      case KARMA_WITHDRAW:
       return withdraw(sender, command, label, arguments);
      default:
       incorrectArgument(sender, arguments[0]);
     }
    } else {
     return tooLittleArguments(sender);
    }
  }
  return false;
 }

 public boolean balance(CommandSender sender, Command command, String label, String[] arguments) {
  if (arguments.length <= 2) {
   if (sender instanceof Player) {
    sender.sendMessage(secondary + "You have " + primary + getKarmaPoints(((Player) sender).getUniqueId().toString()) + secondary + " Karma Points");
    return true;
   } else {
    return meantForPlayers(sender);
   }
  } else {
   return tooManyArguments(sender);
  }
 }

 public boolean deposit(CommandSender sender, Command command, String label, String[] arguments) {
  if (arguments.length > 1) {
   switch (arguments[1]) {
    case KARMA_DEPOSIT_LIST:
     if (arguments.length == 2) {
      sender.sendMessage(secondary + "---" + primary + " Depositable Items " + secondary + "---");
      List<String> depositables = getConfig().getStringList("filter.bank.items");
      List<String> values = getConfig().getStringList("filter.bank.values");
      for (int i = 0; i < depositables.size(); i++) {
       Material itemMat = Material.getMaterial(depositables.get(i));
       sender.sendMessage(primary + "Points: " + values.get(i) + secondary + " Item: " + itemMat.name());//userFriendlyName(itemMat.name()));
      }
      return true;
     } else {
      return tooManyArguments(sender);
     }
    default:
     return incorrectArgument(sender, arguments[1]);
   }
  } else {
   if (sender instanceof Player) {
    Player player = (Player) sender;
    String uuid = player.getUniqueId().toString();
    ItemStack donation = player.getInventory().getItemInMainHand();
    if (isValidItem(true, donation)) {
     List<String> items = getConfig().getStringList("filter.bank.items");
     List<Integer> values = getConfig().getIntegerList("filter.bank.values");
     int key = items.indexOf(donation.getType().toString());

     player.getInventory().removeItem(donation);
     setKarmaPoints(uuid, getKarmaPoints(uuid) + (donation.getAmount() * values.get(key)));
     setDonationItemAmount(donation.getType().toString(), getDonationItemAmount(donation.getType().toString()) + donation.getAmount());
     sender.sendMessage(primary + "Thank you for your donation to the Karma Bank");
     String message = secondary + "You have donated " + donation.getAmount() + " " + donation.getType().name();//userFriendlyName(donation.getType().name());
     if (donation.getAmount() != 1) {
      message += "s";
     }
     sender.sendMessage(message);
     message = secondary + "And earned " + (donation.getAmount() * values.get(key)) + " Karma Point";
     if ((donation.getAmount() * values.get(key)) != 1) {
      message += "s";
     }
     sender.sendMessage(message);
    } else {
     sender.sendMessage(ChatColor.RED + "We're sorry, but your donation is not valid");
    }
    return true;
   } else {
    return meantForPlayers(sender);
   }
  }
 }

 public boolean withdraw(CommandSender sender, Command command, String label, String[] arguments) {
  if (arguments.length > 1) {
   switch (arguments[1]) {
    case KARMA_WITHDRAW_LIST:
     if (arguments.length <= 2) {
      sender.sendMessage(secondary + "---" + primary + " Withdrawable Items " + secondary + "---");
      for (Object key : karmadonations.keySet()) {
       String itemKey = (String) key;
       int amount = Integer.parseInt(karmadonations.getProperty(itemKey));
       if (amount > 0) {
        sender.sendMessage(primary + "[Donation] Amount: " + amount + secondary + " Item: " + itemKey);
       }
      }
      List<String> prizes = getConfig().getStringList("filter.prizes.items");
      List<Integer> prices = getConfig().getIntegerList("filter.prizes.values");
      for (int i = 0; i < prizes.size(); i++) {
       sender.sendMessage(secondary + "[Prize] Price: " + prices.get(i) + primary + " Item: " + prizes.get(i));
      }
     } else {
      return tooManyArguments(sender);
     }
     return true;
    case KARMA_WITHDRAW_PRIZE:
     if (arguments.length >= 3) {
      if (arguments.length <= 4) {
       if (sender instanceof Player) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        ItemStack prize = new ItemStack(Material.getMaterial(arguments[2]));
        if (isValidItem(false, prize)) {
         int points = getKarmaPoints(uuid);
         int amount = 1;
         if (arguments.length == 4) {
          amount = Integer.parseInt(arguments[3]);
         }
         int pricePerItem = getConfig().getIntegerList("filter.prizes.values").get(getConfig().getStringList("filter.prizes.items").indexOf(arguments[2]));
         int maxAmount = points / pricePerItem;
         if (amount > maxAmount) {
          sender.sendMessage(ChatColor.YELLOW + "You do not have enough Karma Points to buy all of those, your amount has been reduced to " + maxAmount);
          amount = maxAmount;
         }
         setKarmaPoints(uuid, points - (amount * pricePerItem));
         prize.setAmount(amount);
         ArrayList<String> lore = new ArrayList();
         lore.add(getConfig().getString("karmaprizelore"));
         ItemMeta meta = prize.getItemMeta();
         if (meta != null) {
          meta.setLore(lore);
          prize.setItemMeta(meta);
         }
         player.getInventory().addItem(prize);
         String message = primary + "Good job, you have earned " + secondary + amount + " " + this.userFriendlyName(arguments[2]);
         if (amount != 1) {
          message += "s";
         }
         sender.sendMessage(message);
         return true;
        } else {
         sender.sendMessage(ChatColor.RED + arguments[2] + " is not a valid prize item");
         return true;
        }
       } else {
        return meantForPlayers(sender);
       }
      } else {
       return tooManyArguments(sender);
      }
     } else {
      return tooLittleArguments(sender);
     }
    case KARMA_WITHDRAW_DONATION:
     if (arguments.length >= 3) {
      if (arguments.length <= 4) {
       if (sender instanceof Player) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        ItemStack prize = new ItemStack(Material.getMaterial(arguments[2]));
        int maxAmount = getDonationItemAmount(prize.getType().toString());
        if (isValidItem(true, prize)) {
         if (maxAmount <= 0) {
          sender.sendMessage(ChatColor.RED + "Sorry, but we do not have that item at the moment, try another item or ask later.");
          return true;
         }
        }
        if (maxAmount > 0) {
         int amount = 1;
         if (arguments.length == 4) {
          amount = Integer.parseInt(arguments[3]);
         }

         if (amount > maxAmount) {
          sender.sendMessage(ChatColor.YELLOW + "We do not have all of those, your amount has been reduced to " + maxAmount);
          amount = maxAmount;
         }
         prize.setAmount(amount);
         setDonationItemAmount(prize.getType().toString(), maxAmount - amount);
         ArrayList<String> lore = new ArrayList();
         lore.add(getConfig().getString("karmaitemlore"));
         ItemMeta meta = prize.getItemMeta();
         if (meta != null) {
          meta.setLore(lore);
          prize.setItemMeta(meta);
         }
         player.getInventory().addItem(prize);
         String message = primary + "Here, have these " + secondary + amount + " " + userFriendlyName(prize.getType().name());
         if (amount != 1) {
          message += "s";
         }
         sender.sendMessage(message);
         return true;
        } else {
         sender.sendMessage(ChatColor.RED + arguments[2] + " is not a valid donated item");
         return true;
        }
       } else {
        return meantForPlayers(sender);
       }
      } else {
       return tooManyArguments(sender);
      }
     } else {
      return tooLittleArguments(sender);
     }
    default:
     return incorrectArgument(sender, arguments[1]);
   }
  } else {
   return tooLittleArguments(sender);
  }
 }

 public boolean meantForPlayers(CommandSender sender) {
  sender.sendMessage(ChatColor.RED + "This command is meant for players");
  if (sender.isOp()) {
   sender.sendMessage(ChatColor.RED + "You are a " + sender.getClass());
  }
  return true;
 }

 public boolean incorrectArgument(CommandSender sender, String argument) {
  sender.sendMessage(ChatColor.RED + "\"" + argument + "\" is not a valid argument");
  return true;
 }

 public boolean tooLittleArguments(CommandSender sender) {
  sender.sendMessage(ChatColor.RED + "You've entered in not enough arguments");
  return true;
 }

 public boolean tooManyArguments(CommandSender sender) {
  sender.sendMessage(ChatColor.RED + "You've entered in too many arguments");
  return true;
 }

 protected int getKarmaPoints(String uuid) {
  String number = karmapoints.getProperty(uuid);
  int returned = 0;
  if (number != null) {
   returned = Integer.parseInt(number);
  }
  return returned;
 }

 protected int getDonationItemAmount(String itemid) {
  String number = karmadonations.getProperty(itemid);
  int returned = 0;
  if (number != null) {
   returned = Integer.parseInt(number);
  }
  return returned;
 }

 protected void setKarmaPoints(String uuid, int amount) {
  karmapoints.setProperty(uuid, "" + amount);
 }

 protected void setDonationItemAmount(String itemid, int amount) {
  karmadonations.setProperty(itemid.toUpperCase(), "" + amount);
 }

 protected boolean isValidItem(boolean donation, ItemStack item) {
  boolean returned = false;
  String pool = "filter.";
  if (donation) {
   pool += "bank.";
  } else {
   pool += "prizes.";
  }
  boolean isContained = getConfig().getStringList(pool + "items").contains(item.getType().toString());
  if (isContained) {
   returned = true;
   if (donation) {
    if (item.getItemMeta() != null) {
     List<String> lore = item.getItemMeta().getLore();
     if (lore != null) {
      if (lore.contains(getConfig().getString("karmaitemlore"))) {
       returned = false;
      }
     }
    }
   }
  }
  return returned;
 }

 public String userFriendlyName(String oldName) {
  return oldName.replace("_", " ").toLowerCase();
 }

}
