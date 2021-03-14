using Godot;
using Newtonsoft.Json;
using Simplerpgkataclient.Network;
using Simplerpgkataclient.Scripts.Scene;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;

public enum CharacterClass
{
	Unknown = 0,
	Paladin = 1,
	Wizard = 2,
	Rogue = 3
}


public class Character
{
	public double InitHp { get; set; }
	public double Hp { get; set; }
	public int Id { get; set; }
	public int Level { get; set; }
	public double Resistance { get; set; }
	public CharacterPosition Position { get; set; }
	public bool Dead { get { return this.Hp == 0; } }

	public bool IsPlayer { get; set; }
	public string Username { get; set; }

	public CharacterClass CharacterClass { get; set; }
}

public class CharacterPosition
{

	public CharacterPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float x { get; set; }
	public float y { get; set; }
}

public class Battlefield : Node2D
{
	private CharacterNode playerNode;
	// List of character exclude the current player
	private List<CharacterNode> characterNodes = new List<CharacterNode>();
	
	private CharacterNode genericNodeToClone;
	private IEnumerable<Character> enemies;
	private Character player;

	public override void _Ready()
	{
 
		this.genericNodeToClone = this.GetNode("NpcNode") as CharacterNode;
		this.playerNode = this.GetNode("PlayerNode") as CharacterNode;

		var characters  = this.RetrieveConnectedCharacters();
		this.LoadCharacters(characters);
		this.AddCharactersSprites();

		WebSocketService webSocketService = WebSocketService.GetInstance();
		var err = webSocketService.Connect("simple-rpg-kata-ws");

		if (err != Error.Ok)
		{
			GD.Print("Unable to connect");
			this.SetProcess(false);
		}			
		else
		{
			webSocketService.ConnectionClosedEvent += this.ConnectionClosed;
			webSocketService.ConnectionEnstablishedEvent += this.ConnectionEnstablished;
			webSocketService.DataReceivedEvent += this.DataChanged;
		}
			
	}

	private void ConnectionEnstablished(object sender, object e)
	{
		GD.Print("Connection enstablished");
	}

	private void ConnectionClosed(object sender, object e)
	{
		GD.Print("Connection closed");
	}

	private void DataChanged(object sender, object e)
	{
		// For now  this method only receive a list of character position
		string message = e as string;
		List<WebSocketParams> webSocketParamsList =  JsonConvert.DeserializeObject<List<WebSocketParams>>(message);

		foreach(var param in webSocketParamsList)
		{
			CharacterNode node = this.characterNodes.FirstOrDefault(n => n.Character.Id == param.characterId && !n.Character.Dead);

			if(node != null)
				node.UpdateCharacter(param);
		}
	}

	public override void _Process(float delta)
	{
		base._Process(delta);
		WebSocketService.GetInstance().Poll();
	}

	private void LoadCharacters(IEnumerable<Character> characters)
	{
		this.player = characters.SingleOrDefault(character => character.Username == Session.Username && (int)character.CharacterClass == Session.ClassId);
		this.enemies = characters.Where(character => character != this.player);
	}

	private void AddCharactersSprites()
	{
		this.playerNode.Initialize(this.player);
		this.characterNodes.Add(this.playerNode);

		foreach (Character character in this.enemies)
			this.AddCharacterSprite(character);

	}

	private void AddCharacterSprite(Character character)
	{
		CharacterNode characterSprite = this.genericNodeToClone.Duplicate() as CharacterNode;
		this.AddChild(characterSprite);

		characterSprite.Initialize(character);

		characterSprite.Show();

		this.characterNodes.Add(characterSprite);
	}

	private List<Character> RetrieveConnectedCharacters()
	{
		using (HttpClient client = new HttpClient())
		{
			HttpResponseMessage response = client.GetAsync("http://simple-rpg-kata.herokuapp.com/api/character/all/connected").Result;

			string json = response.Content.ReadAsStringAsync().Result;

			return JsonConvert.DeserializeObject<List<Character>>(json);
		}
	}

	public CharacterNode GetSelectedTarget()
	{
		foreach (Node children in this.GetChildren())
		{
			if (children is NpcNode)
			{
				NpcNode npc = children as NpcNode;
				if (npc.IsSelected)
					return npc;
			}
		}
		return null;
	}


	public void DeselectAllEnemies()
	{
		foreach (Node children in this.GetChildren())
		{
			if (children is NpcNode)
			{
				NpcNode npc = children as NpcNode;
				npc.Deselect();
			}
		}
	}

	public void OnNext(Node value)
	{
		GD.Print(value);
   //     throw new NotImplementedException();
	}

	public void OnError(Exception error)
	{
		GD.Print(error);
		//   throw new NotImplementedException();
	}

	public void OnCompleted()
	{
		GD.Print("on completed");
		// throw new NotImplementedException();
	}
}
