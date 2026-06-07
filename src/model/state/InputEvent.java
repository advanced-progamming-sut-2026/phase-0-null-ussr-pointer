package model.state;

public sealed interface InputEvent permits CheatCmd, AdvanceTimeCmd, BoostPlantCmd, CollectSunCmd, FeedPlantCmd, PlantCmd, PluckCmd, CollectItemCmd {}

