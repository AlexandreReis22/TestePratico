using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerInput : MonoBehaviour
{
    public PlayerManager pManager;

    void Start()
    {
        pManager = GetComponent<PlayerManager>();   
    }

    
    void Update()
    {
        pManager._h = Input.GetAxisRaw("W");
        pManager._v = Input.GetAxisRaw("Horizontal");
        if (Input.GetButtonDown("Fire1"))
        {
            pManager.TiroSpawn();
            if (!ManagerGame._iniciou)
            {
                ManagerGame._iniciou = true;
            }
        } 
        else if (Input.GetButtonDown("Fire2"))
        {
            pManager.tiroLateral();
        }

    }
}
