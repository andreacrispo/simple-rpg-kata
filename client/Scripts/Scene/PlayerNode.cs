using Godot;
using System;

public class PlayerNode : CharacterNode
{
	const int walkSpeed = 200;
	Vector2 velocity;

	public override void _PhysicsProcess(float delta)
	{
		base._PhysicsProcess(delta);


		if (Input.IsActionPressed("ui_left"))
		{
			velocity.x = -walkSpeed;
		}
		else if (Input.IsActionPressed("ui_right"))
		{
			velocity.x = walkSpeed;
		}
		else if (Input.IsActionPressed("ui_up"))
		{
			velocity.y = -walkSpeed;
		}
		else if (Input.IsActionPressed("ui_down"))
		{
			velocity.y = walkSpeed;

		}
		else
		{
			velocity.x = 0;
			velocity.y = 0;
		}

		// We don't need to multiply velocity by delta because "MoveAndSlide" already takes delta time into account.

		// The second parameter of "MoveAndSlide" is the normal pointing up.
		// In the case of a 2D platformer, in Godot, upward is negative y, which translates to -1 as a normal.
		MoveAndSlide(velocity, new Vector2(0, -1));
	}
}
