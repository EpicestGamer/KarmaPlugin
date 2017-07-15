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
import java.util.UUID;
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
public class Karma extends JavaPlugin {

 public static final Logger LOG = Logger.getLogger("Minecraft");

 public static final String KARMA = "karma";

 public static final String KARMA_DEPOSIT = "deposit";
 public static final String KARMA_DEPOSIT_LIST = "list";

 public static final String KARMA_BALANCE = "balance";

 public static final String KARMA_WITHDRAW = "withdraw";
 public static final String KARMA_WITHDRAW_LIST = "list";
 public static final String KARMA_WITHDRAW_PRIZE = "prize";
 public static final String KARMA_WITHDRAW_DONATION = "donation";

 Properties karmapoints = new Properties();

 Properties karmadonations = new Properties();

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
  String commandString = command.getName();

  Player player = null;
  UUID playerUuid = null;
  String uuidString = null;
  if (sender instanceof Player) {
   player = (Player) sender;
   playerUuid = player.getUniqueId();
   uuidString = playerUuid.toString();
  }

  switch (commandString.toLowerCase()) {
   case KARMA:
    switch (arguments[0].toLowerCase()) {
     case KARMA_DEPOSIT:
      if (arguments.length < 2) {
       //if (player != null) {
       if (player == null) {
        sender.sendMessage(ChatColor.RED + "Only players can run item-related commands");
        return true;
       } else {
        ItemStack donation = player.getInventory().getItemInMainHand();
        String itemname = donation.getType().toString();
        boolean whitelist = getConfig().getBoolean("filter.bank.whitelist");
        List<String> depositables = getConfig().getStringList("filter.bank.items");
        int itemNumber = depositables.indexOf(itemname);
        System.out.println(itemname);
        boolean itemContained = itemNumber != -1;
        boolean isKarmaItem = false;
        if (donation.getItemMeta().getLore() != null) {
         isKarmaItem = donation.getItemMeta().getLore().contains((String) getConfig().get("karmaitemlore"));
        }
        LOG.info("" + isKarmaItem + itemContained + whitelist);
        System.out.println("" + isKarmaItem + itemContained + whitelist);
        if ((((whitelist) && (itemContained)) || ((!whitelist) && (!itemContained))) && (!isKarmaItem)) {
         int donationSize = donation.getAmount();
         List<String> values = getConfig().getStringList("filter.bank.values");
         int pointsValue = Integer.parseInt(values.get(itemNumber));
         int points = donationSize * pointsValue;
         int pointsPrevious = 0;
         if (karmapoints.containsKey(uuidString)) {
          pointsPrevious = Integer.parseInt(karmapoints.getProperty(uuidString));
         }
         karmapoints.setProperty(uuidString, Integer.toString(points + pointsPrevious));
         int donationsPrevious = 0;
         if (karmadonations.containsKey(uuidString)) {
          donationsPrevious = Integer.parseInt(karmadonations.getProperty(itemname));
         }
         karmadonations.setProperty(itemname, Integer.toString(donationsPrevious + donationSize));
         String message = ChatColor.GREEN + "You have donated " + donationSize + " " + donation.getType().name();
         player.getInventory().remove(donation);
         if (donationSize != 1) {
          message += "s and earned " + points + " Karma Point";
         } else {
          message += " and earned " + points + " Karma Point";
         }
         if (points != 1) {
          message += "s";
         }
         sender.sendMessage(message);
         return true;
        } else {
         sender.sendMessage(ChatColor.RED + "The item you are holding is not a valid donation");
         return true;
        }
       }
      } else {
       switch (arguments[1].toLowerCase()) {
        case KARMA_DEPOSIT_LIST:
         sender.sendMessage("--- Depositable Items ---");
         List<String> depositables = getConfig().getStringList("filter.bank.items");
         for (String item : depositables) {
          sender.sendMessage(item);
         }
         return true;
        default:
         break;
       }
      }
      break;
     case KARMA_BALANCE:
      String message = "You have ";
      if (karmapoints.containsKey(uuidString)) {
       if (Integer.parseInt(karmapoints.getProperty(uuidString)) != 1) {
        message += karmapoints.getProperty(uuidString) + " Karma Points";
       } else {
        message += "a Karma Point";
       }
      } else {
       message += "0 Karma Points";
      }
      sender.sendMessage(message);
      return true;
     case KARMA_WITHDRAW:
      switch (arguments[1]) {
       case KARMA_WITHDRAW_LIST:
        sender.sendMessage("--- Withdrawable Items ---");

        for (Object key : karmadonations.keySet()) {
         String itemKey = (String) key;
         int amount = Integer.parseInt(karmadonations.getProperty(itemKey));
         if (amount > 0) {
          sender.sendMessage("[Donation] Amount: " + amount + " Item: " + itemKey);
         }
         List<String> prizes = getConfig().getStringList("filter.prizes.items");
         List<String> prices = getConfig().getStringList("filter.prizes.values");
         for (int i = 0; i < prizes.size(); i++) {
          sender.sendMessage("[Prize] Price: " + prices.get(i) + " Item: " + prizes.get(i));
         }
        }
        return true;
       case KARMA_WITHDRAW_DONATION:
        if (player != null) {
         if (arguments.length >= 4) {
          String materialName = arguments[3].toUpperCase();
          Material itemMaterial = null;
          if (karmadonations.containsKey(materialName)) {
           itemMaterial = Material.valueOf(materialName);
          } else {
           sender.sendMessage(ChatColor.RED + "Your requested item is not in the donation pile, try \"/karma withdraw list\"");
           return true;
          }
          int donationPile = Integer.parseInt(karmadonations.getProperty(materialName));
          if (donationPile > 0) {
           int amount = 1;
           if (arguments.length > 4) {
            amount = Integer.parseInt(arguments[4]);
           }
           if (amount > donationPile) {
            sender.sendMessage("We do not have enough " + materialName + " for you, your amount has be reduced to " + donationPile);
            amount = donationPile;
           }
           int afterPile = donationPile = amount;
           karmadonations.setProperty(materialName, "" + afterPile);
           ItemStack itemDonation = new ItemStack(itemMaterial, amount);
           List<String> lores = new ArrayList<String>();
           lores.add(getConfig().getString("karmaitemlore"));
           ItemMeta im = itemDonation.getItemMeta();
           im.setLore(lores);
           itemDonation.setItemMeta(im);
           player.getInventory().addItem(itemDonation);
          } else {
           sender.sendMessage(ChatColor.RED + "Your requested item is not in the donation pile, try \"/karma withdraw list\"");
          }
         } else {
          return notEnoughArguments(sender);
         }
        } else {
         System.out.println(sender);
         return playerItemCommand(sender);
        }
        return true;
       case KARMA_WITHDRAW_PRIZE:
        if (sender instanceof Player) {
         if (arguments.length >= 4) {
          int amount = 1;
          if (arguments.length > 4) {
           amount = Integer.parseInt(arguments[4]);
          }
          String materialName = arguments[3].toUpperCase();
          sender.sendMessage(materialName);
          int points = 0;
          if (karmapoints.getProperty(uuidString) != null) {
           points = Integer.parseInt(karmapoints.getProperty(uuidString));
          }

          int listNumber = getConfig().getStringList("filter.prizes.items").indexOf(materialName);

          boolean isContained = listNumber != -1;
          if (isContained) {
           int itemValue = Integer.parseInt(getConfig().getStringList("filter.prizes.values").get(listNumber));
           int maxPrizes = points / itemValue;
           if (amount > maxPrizes) {
            sender.sendMessage("You do not have enough Karma Points, the amount has been reduced to " + maxPrizes);
            amount = maxPrizes;
           }
           int price = amount * itemValue;
           int afterPoints = points - price;
           Material itemMaterial = Material.valueOf(materialName);
           ItemStack items = new ItemStack(itemMaterial, amount);
           player.getInventory().addItem(items);
           karmapoints.setProperty(uuidString, "" + afterPoints);
          } else {
           sender.sendMessage(ChatColor.RED + "Your requested item is not in the prize pile, try \"/karma withdraw list\"");
          }
         } else {
          return notEnoughArguments(sender);
         }
        } else {
         return playerItemCommand(sender);
        }
        return true;
       default:
        return invalidSubCommand(sender, arguments[1]);
      }
     default:
      return invalidSubCommand(sender, arguments[0]);
    }
   default:
    return false;
  }
 }

 public boolean invalidSubCommand(CommandSender sender, String argument) {
  sender.sendMessage(ChatColor.RED + "\"" + argument + "\" is not a valid sub-command");
  return true;
 }

 public boolean playerItemCommand(CommandSender sender) {
  sender.sendMessage(ChatColor.RED + "Only players can run item-related commands");
  return true;
 }

 public boolean notEnoughArguments(CommandSender sender) {
  sender.sendMessage(ChatColor.RED + "Only players can run item-related commands");
  return true;
 }

}
