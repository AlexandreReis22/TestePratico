using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerManager : MonoBehaviour
{
    [Header("Movimentação")]

    public float           _v;
    public float           _h;
    [SerializeField]
    private float          _speedMove;
    [SerializeField]
    private float          _speedRotate;

    [Header("Tiro da Frente")]

    public float           _fireTime;
    public float           _fireLateral;   
    [SerializeField]
    private GameObject     _tiro;
    [SerializeField]
    private Transform      _SpawnTiro;

    [Header("Tiro Da Lateral")]

    [SerializeField]
    private GameObject[]  _tiroLateral;    
    [SerializeField]
    private Transform[]   _SpawnLateral;

    [Header("Vida")]

    public int               _vida;
    public Slider            _barraVida;
    public GameObject        _objVida;    
    public float             _timeBar, _timeInvencivel;
    public bool              _hitCheck, _morreu, _invencivel;  
    
    [Header("animacoes")]
    private Animator anim;

    void Start()
    {
        anim= GetComponent<Animator>();
       
    }

    void Update()
    {
        
        if (ManagerGame._iniciou)
        {
            Move();
            Rotate();
            _fireTime += 1 * Time.deltaTime;
            _fireLateral += 1 * Time.deltaTime;            
        }            
        
        if(_hitCheck)
        {
            _timeBar += 1 * Time.deltaTime;            
        }
        if(_timeBar > 2)
        {
            _objVida.SetActive(false);
            _timeBar = 0;            
        }
        animacao();
        invencivel();
    }   

    public void Move()
    {       
        Vector2 temp = new Vector2(0, _h);
        transform.Translate((temp * _speedMove * Time.deltaTime), Space.Self);               
    }
    public void Rotate() 
    {       
        if (_h != 0)
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
    void animacao()
    {
        anim.SetBool("vida2", _vida == 2);
        anim.SetBool("Vida1", _vida == 1);
        anim.SetBool("Morreu", _vida == 0);        
    }
    void dano()
    {
        if (_hitCheck)
        {
            _vida--;
            _barraVida.value = _vida;
            _objVida.SetActive(true);
            _invencivel = true;            
        }        
    }
    void invencivel()
    {
        if (_invencivel)
        {
            _timeInvencivel += 1 * Time.deltaTime;
            if (_timeInvencivel > 2f)
            {
                _invencivel = false;
                _timeInvencivel = 0;
                _hitCheck = false;
            }
        }        
    }    
    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.tag.Equals("TiroInimigo"))
        {
            _hitCheck = true;
            if (!_invencivel)
            {
                dano();
            }
        }
    }
    private void OnCollisionEnter2D(Collision2D collision)
    {
        if (collision.collider.tag.Equals("Enemy") || collision.collider.tag.Equals("Chaser"))
        {
            _vida= 0;
        }
    }
}

