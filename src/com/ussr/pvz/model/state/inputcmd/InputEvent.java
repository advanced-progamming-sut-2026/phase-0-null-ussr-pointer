package com.ussr.pvz.model.state.inputcmd;

public sealed interface InputEvent permits CheatCmd, AdvanceTimeCmd, BoostPlantCmd, CollectSunCmd, FeedPlantCmd, PlantCmd, PluckCmd, CollectItemCmd {}

