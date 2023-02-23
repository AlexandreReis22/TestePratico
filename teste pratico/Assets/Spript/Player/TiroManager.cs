using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TiroManager : MonoBehaviour
{    
    [SerializeField]
    private float          _SpeedTiro;
    [SerializeField]
    private Animator       _anim;    
    public bool           _destoy;
    public float          _SpeedDestroyAuto;
    public int            _eixoX, _eixoY;
    public bool           _tiroInimigo;

    void Start()
    {
       _anim = GetComponent<Animator>();
    }
    
    void Update()
    {
        _SpeedDestroyAuto += 1 * Time.deltaTime;
        if (_destoy || _SpeedDestroyAuto > 3)
        {
            Destroy(this.gameObject);            
        }
        moveTiro();
    }    

    void moveTiro()
    {
        Vector2 temp = new Vector2(_eixoX, _eixoY);
        transform.Translate((temp * Time.deltaTime * _SpeedTiro));        
    }
    
    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (_tiroInimigo)
        {
            _anim.SetBool("explosao", collision.gameObject.tag == "Parede" || collision.gameObject.tag == "Player");
        }
        else
        {
            _anim.SetBool("explosao", collision.gameObject.tag == "Parede" || collision.gameObject.tag == "Enemy");
        }
        _SpeedTiro = 0;
    }
}
