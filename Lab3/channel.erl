-module(channel).
-export([handle/2, initial_state/1]).
-include_lib("./defs.hrl").

%% inititial_state/2 and handle/2 are used togetger with the genserver module,
%% explained in the lecture about Generic server.

% Produce initial state
initial_state(ChannelName) ->
    #channel_st{name = ChannelName}.


handle(St,{join,PID}) ->
    io:fwrite("Someone wants to join the channel in the process itself: ~p~n", [St]),
    case lists:member(PID,St#channel_st.cUsers) of
        true ->
            {reply,user_already_joined,St};
        false ->
            NewState = St#channel_st{cUsers = [PID] ++ St#channel_st.cUsers},
            {reply,joined,NewState}
    end;

handle(St,{leave, PID}) ->
    case lists:member(PID,St#channel_st.cUsers) of
        true ->
            NewList = lists:delete(PID,St#channel_st.cUsers),
            NewState = St#channel_st{cUsers = NewList},
            {reply, left, NewState};
        false ->
            {reply, user_not_joined, St}
    end;

handle(St,{message,Msg,Nick,PID}) ->
    [ sendMessage(E, Nick , St#channel_st.name, Msg) || E <- St#channel_st.cUsers, E /= PID],
    {reply, ok,St};

% handle(St,{message, Msg, Nick, PID} ->
%     case lists:any(fun(E) -> E == ClientName end, St#chatroom_st.clients) of
%       true ->
%         NewSt = St,
%         CallClients = fun(PID) ->
%           case PID /= ClientId of
%             true -> spawn(fun() -> genserver:request(PID, {incoming_msg, Channel, ClientName,Msg}) end);
%             false -> 0
%           end
%         end,
%         lists:map(CallClients, St#chatroom_st.clientIds),
%         Response = "success";
%       false ->
%         NewSt = St,
%         Response = user_not_joined
%     end

handle(St, Request) ->
    io:fwrite("In channel.erl, Shouldn't have gotten here, derp: ~p~n", [Request]),
    Response = "hi!",
    io:fwrite("Server is sending: ~p~n", [Response]),
    {reply, joined, St}.

sendMessage(PID,From,Channel,Msg) ->
    io:fwrite("Spreading message: ~p~n", [PID]),
    spawn (fun() -> genserver:request(PID,{incoming_msg, Channel, atom_to_list(From), Msg}) end).
