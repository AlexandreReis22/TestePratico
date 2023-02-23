using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class chaser : MonoBehaviour
{
    public Transform _player;
    public float     _speed;    
    public Animator  _anim;   
    [Header("vida")]
    [SerializeField]
    private int _vida;
    [SerializeField]
    private bool _checkHit, _morreu;
    [SerializeField]
    private float _timeBar;
    [SerializeField]
    private Slider _barraVida;
    [SerializeField]
    private GameObject _objVida;

    void Start()
    {
        _anim= GetComponent<Animator>();
        _player = FindObjectOfType<PlayerManager>().transform;
    }

    void Update()
    {
        animator();
        if (_morreu)
        {
            Spawn._limiteSpawm--;
            ManagerGame._pontos += 10;
            Destroy(gameObject);
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
    }

    void FixedUpdate()
    {
        transform.Translate((Vector2.up * _speed * Time.deltaTime), Space.Self);                       
        Vector2 TempPlayer = _player.position;
        Vector2 dir = new Vector2(TempPlayer.x - transform.position.x, TempPlayer.y - transform.position.y);
        transform.up = dir * Time.deltaTime;        
    }
    void animator()
    {
        _anim.SetBool("vida2", _vida == 2);
        _anim.SetBool("vida1", _vida == 1);
        _anim.SetBool("morreu", _vida == 0);
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
    private void OnCollisionEnter2D(Collision2D collision)
    {
        if (collision.collider.tag.Equals("Player"))
        {
            _vida = 0;
            ManagerGame._pontos -= 10;
        }
    }
    private void OnTriggerEnter2D(Collider2D collision)
    {        
        if (collision.tag.Equals("Tiro"))
        {
            _checkHit = true;
            dano();
        }
    }
}
