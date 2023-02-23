using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Shooter : MonoBehaviour
{
    [Header("movimento")]

    [SerializeField]
    private float         _speedMove;    
    public float          _speedRotate;
    [SerializeField]
    private float         _rotate1,_rotate2;
    [SerializeField]
    private bool          _starRotate, _starRotate2;

    [Header("Tiro")]

    [SerializeField]
    private GameObject    _tiro;
    private bool          _Disparo;
    [SerializeField]
    private float         _timeTiro;
    [SerializeField]
    private Transform[]   _posTiro, _rangeTiro;

    [Header("vida")]
    [SerializeField]
    private int           _vida;
    [SerializeField]
    private bool          _checkHit, _morreu;
    [SerializeField]
    private float         _timeBar;
    [SerializeField]
    private Slider        _barraVida;
    [SerializeField]
    private GameObject    _objVida;    

    [Header("animacao")]
    [SerializeField]
    private Animator      _anim;

    void Start()
    {
        _anim = GetComponent<Animator>();
    }
    
    void Update()
    {
        if (ManagerGame._iniciou)
        {
            _timeTiro += 1 * Time.deltaTime;
            _Disparo = Physics2D.Linecast(transform.position, _rangeTiro[0].position, 1 << LayerMask.NameToLayer("Player")) || Physics2D.Linecast(transform.position, _rangeTiro[1].position, 1 << LayerMask.NameToLayer("Player"));            
            
            
            if (_Disparo && _timeTiro > 4)
            {
                Instantiate(_tiro, _posTiro[0].position, transform.localRotation);
                Instantiate(_tiro, _posTiro[1].position, transform.localRotation);
                _timeTiro = 0;
            }
            if (_checkHit)
            {
                _timeBar += 1 * Time.deltaTime;
            }
            if (_timeBar > 2)
            {
                _objVida.SetActive(false);
                _timeBar = 0;
                _checkHit = false;
            }           
            rotateCheck();
            move();
            anim();
            destroyShooter();
        }                    
    }
    void rotateShip()
    {
        transform.Rotate(Vector3.forward * _speedRotate * Time.deltaTime);
    }
    void rotateCheck()
    {
        if (_starRotate)
        {
            rotateShip();
            if (transform.eulerAngles.z >= _rotate1 && transform.eulerAngles.z <= _rotate2 && !_starRotate2)
            {
                _starRotate = false;

            }
        }
        else if (_starRotate2)
        {
            rotateShip();
            if (transform.eulerAngles.z >= _rotate2 && !_starRotate)
            {
                _starRotate2 = false;
            }
        }
    }
    void move()
    {
        Vector2 tempVector2 = new Vector2(0, -1);
        transform.Translate((tempVector2 * _speedMove * Time.deltaTime));
    }
    void dano()
    {
        if (_checkHit)
        {
            _vida--;
            _barraVida.value = _vida;
            _objVida.SetActive(true);            
        }
    }
    void anim()
    {
        _anim.SetBool("morreu", _vida == 0);
        _anim.SetBool("vida2", _vida == 2);
        _anim.SetBool("vida1", _vida == 1);
    }
    void destroyShooter()
    {
        if (_morreu)
        {
            ManagerGame._pontos += 10;
            Destroy(this.gameObject);
            Spawn._limiteSpawm--;
        }        
    }   
    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.tag == "StarRotate1")
        {
            _starRotate = true;
        } 
        else if (collision.tag == "StarRotate2")
        {
            _starRotate2 = true;
        }
        if (collision.tag.Equals("Tiro"))
        {
            _checkHit = true;
            dano();
        }
    }
}
