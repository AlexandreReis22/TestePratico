using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerManager : MonoBehaviour
{
    [Header("Movimentação")]
    public float _v;
    public float _h;
    [SerializeField]
    private float _speedMove;
    [SerializeField]
    private float _speedRotate;
    [Header("Tiro da Frente")]
    public float _fireTime;
    public float _fireLateral;   
    [SerializeField]
    private GameObject _tiro;
    [SerializeField]
    private Transform _SpawnTiro;
    [Header("Tiro Da Lateral")]
    [SerializeField]
    private GameObject[]  _tiroLateral;    
    [SerializeField]
    private Transform[]  _SpawnLateral;    

    void Start()
    {
        _SpawnLateral[0].Rotate(0, 0, 90);
    }

    void Update()
    {
        Move();
        Rotate();
        _fireTime += 1 * Time.deltaTime;
        _fireLateral += 1 * Time.deltaTime;        
    }

    public void Move()
    {
        Vector2 temp = new Vector2(0,_h);
        transform.Translate((temp * _speedMove * Time.deltaTime), Space.Self); 
    }
    public void Rotate() 
    {
        if(_h != 0)
        {
            transform.Rotate(Vector3.forward * _speedRotate * _v * Time.deltaTime);
        }        
    }
    public void TiroSpawn()
    {
        if(_fireTime > 1)
        {
            Instantiate(_tiro, _SpawnTiro.position, transform.localRotation);
            _fireTime = 0;
        }
    }
    public void tiroLateral()
    {
        if(_fireLateral > 3)
        {
            Instantiate(_tiroLateral[0], _SpawnLateral[0].position, transform.localRotation);
            Instantiate(_tiroLateral[1], _SpawnLateral[1].position, transform.localRotation);
            Instantiate(_tiroLateral[2], _SpawnLateral[2].position, transform.localRotation);
            _fireLateral = 0;
        }
    } 
}

