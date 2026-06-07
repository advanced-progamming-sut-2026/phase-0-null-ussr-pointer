package model.account;

import java.util.List;
import java.util.Map;

public record AccountState(String username , String password , String email , int currentLvl , int coin , int gem , int score , Map<String , Integer> plantLvl , List<NewsItem> personalNews) {
    public static AccountState createNewPlayer(String username , String password , String email){
        return null;
    }
    public AccountState {
        //in this method we can check the validations
    }
}
