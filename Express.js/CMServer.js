const express = require('express');
const filesystem = require('fs');
const app = express();
var fileuploader = require('express-fileupload');
app.use(fileuploader());
const port = 3000

// HTTP STAVOVE KODY
/// USPESNE
const HTTP_OK = 200
const HTTP_CREATED = 201
const HTTP_ACCEPTED = 202
const HTTP_NO_CONTENT = 204

/// CHYBA KLIENTA
const HTTP_BAD_REQUEST = 400
const HTTP_UNAUTHORIZED = 401
const HTTP_FORBIDDEN = 403
const HTTP_NOT_FOUND = 404

const Pool = require('pg').Pool
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'cmsimulator',
  password: '6b7f8538234541e9af7c40559be2a508',
  port: 5432,
})

app.use(express.json());


// REGISTRACIA POUZIVATELA
app.get('/api/user/signup', (req, res) => {
  const username = req.query.username;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const auth_key = req.query.auth_key;
  const user_type = req.query.user_type;
  const salt = req.query.salt;

  // KONTROLA CI POUZIVATEL UZ NEEXISTUJE
  pool.query('SELECT * FROM users WHERE username = $1', [username], (error, results) => {
    if (error) {
      throw error;
    }

    // POUZIVATEL EXISTUJE
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send("USER EXISTS!");
      console.log(Date() + ' ', [username], 'already exists!');
    }
    else {
      var query = 'INSERT INTO users(username, user_type, password_hash, first_name, last_name, salt) VALUES (\'' + [username] + '\', \'' + [user_type] + '\', E\'\\\\x' + auth_key + '\', \'' + [first_name] + '\', \'' + [last_name] + '\', E\'\\\\x' + salt + '\');';
      console.log(query);
      pool.query(query, (err, result) => {
        if (err) { throw err; }
        res.status(HTTP_OK).send('USER WAS ADDED!');
        console.log(Date() + ' ', [username], 'was successfully added to database');
      });
    }
  })
})



// ZMENA HESLA
app.get('/api/user/changePassword', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = req.query.auth_key;
  const new_auth_key = req.query.new_auth_key;

  pool.query('SELECT * FROM users WHERE user_id = $1;', [user_id], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      console.log(Date() + ' ', [results.rows[0].username], 'has changed password.');

      var query = 'UPDATE public.users SET password_hash=E\'\\\\x' + new_auth_key + '\' WHERE user_id=' + user_id + ';';
      pool.query(query, (err, result) => {
        if (error) {
          throw error;
        }
        res.status(HTTP_OK).send('Password changed');
      })
    }
    else {
      console.log(Date() + 'Unable to perform requested operation!');
      res.status(HTTP_NOT_FOUND).send('User was not found!');
    }
  })
})

app.get('/api/user/getCount', (req, res) => {
  pool.query('SELECT count(*) FROM users;', (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(
        {
          count: results.rows[0].count
        }
      );
    }
  });
});


// PRIHLASENIE
app.get('/api/login', (req, res) => {
  const username = req.query.username;
  const password = req.query.auth_key;


  pool.query('select user_id, user_type, username, first_name, last_name, encode(password_hash::bytea, \'hex\') as password_hash, encode(salt::bytea, \'hex\') as salt from users where username = $1;', [username], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      if (results.rows[0].password_hash == password) {
        console.log(Date() + ' ', [username], 'has successfully logged in.');
        res.status(HTTP_OK).send(results.rows[0]);
      }
      else {
        console.log(Date() + ' ', [username], 'was unable to log in.');
        res.status(HTTP_FORBIDDEN).send('Unable to log in!');
      }
    }
    else {
      console.log(Date() + 'Unable to perform requested operation!');
      res.status(HTTP_NOT_FOUND).send('User was not found!');
    }
  })
})

// ZISKANIE SALTU
app.get('/api/login_salt', (req, res) => {
  const username = req.query.username;

  pool.query('SELECT encode(salt::bytea, \'hex\') as salt FROM users WHERE username = $1;', [username], (error, results) => {
    if (error) { throw error }

    if (results.rowCount > 0) {
      res.status(HTTP_OK).send({
        username: username,
        salt: results.rows[0].salt
      })
    }
    else {
      res.status(HTTP_NOT_FOUND).send("Selected user was not found!");
    }
  })
})

app.get('/api/tasks/automata', (req, res) => {
  pool.query('SELECT * FROM automata_tasks;', (error, results) => {
    if (error) { throw error }
    if (results.rowCount > 0) {
      res.send(results.rows);
    }
  })
})



app.get('/api/user/update', (req, res) => {
  const logged_user_id = req.query.logged_user_id;
  const auth_key = '\\x' + req.query.auth_key;
  const user_id = req.query.user_id;
  const first_name = req.query.first_name;
  const last_name = req.query.last_name;
  const password_hash = req.query.password_hash;
  const type = req.query.type;
  const username = req.query.username;

  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND user_type = \'admin\';', [auth_key, logged_user_id], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('UPDATE users SET username=$1, user_type=$2, first_name=$3, last_name=$4 WHERE user_id=$5;', [username, type, first_name, last_name, user_id], (error, result) => {
        if (error) { throw error }
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send({
            admin_id: logged_user_id,
            user_id: user_id,
            first_name: first_name,
            last_name: last_name,
            user_type: type,
            username: username,
            updated: true
          });
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            updated: false
          });
        }
      })
    }
  });
})

app.get('/api/user/delete', (req, res) => {
  const logged_user_id = req.query.logged_user_id;
  const auth_key = '\\x' + req.query.auth_key;
  const user_id = req.query.user_id;
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_id = $2 AND user_type = \'admin\';', [auth_key, logged_user_id], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('DELETE FROM users WHERE user_id = $1;', [user_id], (error, result) => {
        if (error) { throw error }
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send(
            {
              user_id: user_id,
              deleted: true
            }
          );
        }
        else {
          res.status(HTTP_NOT_FOUND).send(
            {
              user_id: user_id,
              deleted: false
            }
          );
        }
      })
    }
  })
})

app.get('/api/user/getUsers', (req, res) => {
  const auth_key = '\\x' + req.query.auth_key;
  const offset = req.query.offset;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_type = \'admin\';', [auth_key], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users LIMIT 20 OFFSET $1;', [offset], (error, foundUsers) => {
        if (error) { throw error }
        if (foundUsers.rowCount > 0) {
          res.status(HTTP_OK).send(foundUsers.rows);
          console.log("All users have been requested!");
        }
      })
    }
    else {
      res.status(HTTP_FORBIDDEN).send("You are not authorised to perform this operation!");
      console.log("Unauthorised request to download all users!");
    }
  })
})

app.get('/api/user/filterUsers', (req, res) => {
  const order_by = req.query.order_by;
  const ascending = req.query.ascending;

  if (order_by == 'username') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY username ASC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY username DESC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  } else if (order_by == 'last_name') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY last_name ASC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY last_name DESC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  } else if (order_by == 'first_name') {
    if (ascending == 'true') {
      pool.query('SELECT * FROM users ORDER BY first_name ASC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    } else {
      pool.query('SELECT * FROM users ORDER BY first_name DESC;', (err, results) => {
        if (err) { throw err }
        if (results.rowCount > 0) {
          res.status(HTTP_OK).send(results.rows);
        }
        else {
          res.status(HTTP_NOT_FOUND).send({
            not_found: true
          });
        }
      })
    }
  }
})

app.get('/api/user/findUsers', (req, res) => {
  const string = req.query.string;
  const find_by = req.query.find_by;

  var search_string = '%' + string + '%';
  if (find_by == 'first_name') {
    pool.query('SELECT * FROM users WHERE first_name LIKE $1;', [search_string], (err, results) => {
      if (err) { throw err }
      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  } else if (find_by == 'last_name') {
    pool.query('SELECT * FROM users WHERE last_name LIKE $1;', [search_string], (err, results) => {
      if (err) { throw err }
      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  } else if (find_by == 'username') {
    pool.query('SELECT * FROM users WHERE username LIKE $1;', [search_string], (err, results) => {
      if (err) { throw err }
      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
      }
      else res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    });
  }

});

app.get('/api/user/getUsersFiltered', (req, res) => {
  const auth_key = '\\x' + req.query.auth_key;
  const last_name = req.query.last_name;
  console.log([auth_key]);
  pool.query('SELECT * FROM users WHERE password_hash = $1 AND user_type = \'admin\';', [auth_key], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('SELECT * FROM users WHERE last_name LIKE \'%' + last_name + '%\';', (error, foundUsers) => {
        if (error) { throw error }
        if (foundUsers.rowCount > 0) {
          res.status(HTTP_OK).send(foundUsers.rows);
          console.log("User search performed!");
        }
      })
    }
    else {
      res.status(HTTP_FORBIDDEN).send("You are not authorised to perform this operation!");
      console.log("Unauthorised request to download all users!");
    }
  })
})

app.get('/api/tasks/updateTaskTimer', (req, res) => {
  const user_id = req.query.user_id;
  const task_id = req.query.task_id;
  const elapsed_time = req.query.elapsed_time;

  pool.query('UPDATE automata_task_results SET time_elapsed=\'$1\' WHERE user_id = $2 AND task_id = $3;', [elpased_time, user_id, task_id], (err, result) => {
    if (error) { throw error }
    if (result.rowCount > 0) {
      res.status(HTTP_OK).send({
        user_id: user_id,
        task_id: task_id,
        elapsed_time: elapsed_time,
        updated: true
      });
    }
  })
})

app.get('/api/tasks/delete', (req, res) => {
  const task_id = req.query.task_id;

  pool.query('SELECT * FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) => {
    if (err) { throw err }
    if (result.rowCount > 0) {
      const filename = "./uploads/automataTasks/" + result.rows[0].task_id + ".cmst";
      filesystem.exists(filename, (exists) => {
        if (exists) {
          filesystem.unlink(filename, (error) => {
            if (error) {
              throw error;
            }
          });
        }
        else {
          console.log("TASK COULD NOT BE DELETED! FILE DOES NOT EXIST!");
        }
      });
    }
  })
  pool.query('DELETE FROM automata_task_results WHERE task_id = $1;', [task_id], (err, result) => {
    if (err) { throw err }
    if (result.rowCount > 0) {
      pool.query('DELETE FROM automata_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
        if (error) { throw error }
        console.log([task_id], "Task has been deleted!");
        res.status(HTTP_OK).send({
          task_id: task_id,
          deleted: true
        });
      })
    }
    else {
      pool.query('DELETE FROM automata_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
        if (error) { throw error }
        console.log([task_id], "Task has been deleted!");
        res.status(HTTP_OK).send({
          task_id: task_id,
          deleted: true
        });
      })
    }
  })
})

app.get('/api/tasks/submit', (req, res) => {
  const task_id = req.query.task_id;
  const user_id = req.query.user_id;
  const task_status = req.query.task_status;
  const submission_time = req.query.submission_time;

  pool.query('SELECT * FROM automata_task_results WHERE user_id = $1 AND task_id = $2 LIMIT 1;', [user_id, task_id], (err, results) => {
    if (err) { throw err }
    if (results.rowCount > 0) {
      pool.query('UPDATE automata_task_results SET submitted=\'true\', submission_date=$1, task_status = $2 WHERE task_id = $3 AND user_id = $4;', [submission_time, task_status, task_id, user_id], (error, result) => {
        if (error) { throw error }
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send({
            task_id: task_id,
            user_id: user_id,
            submitted: true
          });
        }
      });
    }
  })
})

app.post('/api/tasks/upload', (req, res, next) => {
  const file = req.files.task;
  const file_name = req.query.file_name;

  filesystem.mkdir("./uploads/",{ recursive: true }, (err) => {
    if (err) { throw err };
  });
  filesystem.mkdir("./uploads/automataTasks/", { recursive: true }, (err) => {
    if (err) { throw err };
  });
  if (!file) {
    console.log(req);
    console.log("FILE NOT SAVED!");
  }
  else {
    file.mv("./uploads/automataTasks/" + file_name + ".cmst", function (err, results) {
      if (err) throw err;
      res.send({
        success: true,
        message: "File uploaded!"
      });
      console.log("NEW TASK UPLOADED!");
    });
  }

});

app.get('/api/tasks/updateTimer', (req, res) => {
  const user_id = req.query.user_id;
  const task_id = req.query.task_id;
  const time_elapsed = req.query.time_elapsed;

  pool.query('SELECT count(*) from automata_task_results WHERE user_id = $1 AND task_id = $2;', [user_id, task_id], (err, results) => {
    if (err) { throw err }
    if (results.rows[0].count > 0) {
      pool.query('UPDATE automata_task_results SET time_elapsed=$1 WHERE user_id = $2 AND task_id = $3;', [time_elapsed, user_id, task_id], (error, result) => {
        if (error) { throw error }
        if (result.rowCount > 0) {
          res.status(HTTP_OK).send(
            {
              task_id: task_id,
              user_id: user_id,
              time_elapsed: time_elapsed,
              updated: true
            }
          );
        } else {
          res.status(HTTP_OK).send(
            {
              task_id: task_id,
              user_id: user_id,
              time_elapsed: time_elapsed,
              updated: false
            }
          );
        }
      })
    }
    else {
      res.status(HTTP_OK).send(
        {
          task_id: task_id,
          user_id: user_id,
          time_elapsed: time_elapsed,
          updated: true
        }
      );
    }
  })
})

app.get('/api/tasks/add', (req, res) => {
  const task_name = req.query.task_name;
  const task_description = req.query.task_description;
  const time = req.query.time;
  const assigner = req.query.assigner;
  const public_input = req.query.public_input;
  const automata_type = req.query.automata_type;


  pool.query('INSERT INTO automata_tasks(assigner_id, public_input, automata_type, task_description, task_name, time) VALUES ($1, $2, $3, $4, $5, $6);',
    [assigner, public_input, automata_type, task_description, task_name, time], (err, results) => {
      if (err) { throw err; }

      if (results.rowCount > 0) {
        pool.query('SELECT * FROM automata_tasks WHERE assigner_id = $1 AND task_name = $2 AND task_description = $3;', [assigner, task_name, task_description], (error, result) => {
          res.status(HTTP_OK).send(
            {
              task_id: result.rows[0].task_id,
              task_name: task_name,
              task_description: task_description,
              time: time,
              public_input: public_input,
              automata_type: automata_type
            }
          )
        })
      }
    });

})

app.get('/api/tasks/getTasks', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = '\\x' + req.query.auth_key;

  pool.query('select at.*, atr.task_status, at.time - atr.time_elapsed as remaining_time, atr.submitted, atr.submission_date from automata_tasks as at left join (SELECT * from automata_task_results where user_id=$1) as atr on atr.task_id = at.task_id;', [user_id], (err, results) => {

    if (err) { throw err }
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(results.rows);
      console.log(Date(), [user_id], 'has fetched tasks!');
    }
    else {
      res.status(HTTP_NOT_FOUND).send({
        not_found: true
      });
    }
  })
})

app.post('/api/tasks/save', (req, res, next) => {
  const user_id = req.query.user_id;
  const file_name = req.query.file_name;
  const file = req.files.task;
  const makedirPath = "./uploads/" + user_id;
  const path = "./uploads/" + user_id + "/" + file_name;

  filesystem.mkdir("./uploads/", { recursive: true }, (err) => {
    if (err) { throw err };
  });
  filesystem.mkdir(makedirPath, { recursive: true }, (err) => {
    if (err) { throw err };
  });
  if (!file) {
    console.log("FILE NOT SAVED!");
  }
  else {
    file.mv(path, function (err, results) {
      if (err) throw err;
      res.status(HTTP_OK).send({
        success: true,
        message: "File uploaded!"
      });
      console.log([user_id], "TASK WAS SAVED!");
    });
  }
});

app.get('/api/tasks/changeFlag', (req, res) => {
  const task_id = req.query.task_id;
  const user_id = req.query.user_id;
  const task_status = req.query.task_status;

  pool.query('SELECT * FROM automata_task_results WHERE task_id = $1 AND user_id = $2;', [task_id, user_id], (err, result) => {
    if (err) { throw err; }
    if (result.rowCount > 0) {
      if (task_status == "too_late") {
        pool.query('SELECT time FROM automata_tasks where task_id = $1;', [task_id], (er, rs) => {
          if (er) { throw er }
          if (rs.rowCount > 0) {
            const available_time = rs.rows[0].time;
            var interval = "";
            if (available_time.hours) {
              interval += available_time.hours + ":";
            }
            else {
              interval += "00:";
            }

            if (available_time.minutes) {
              interval += available_time.minutes + ":";
            }
            else {
              interval += "00:";
            }

            if (available_time.seconds) {
              interval += available_time.seconds;
            }
            else {
              interval += "00";
            }

            pool.query('UPDATE automata_task_results SET task_status=\'too_late\', time_elapsed = $1 WHERE task_id = $2 AND user_id = $3;', [interval, task_id, user_id], (e, r) => {
              if (e) { throw e }
              if (r.rowCount > 0) {
                res.status(HTTP_OK).send({
                  task_id: task_id,
                  user_id: user_id,
                  task_status: task_status,
                  updated: true
                });
              }
              else {
                res.status(HTTP_OK).send({
                  task_id: task_id,
                  user_id: user_id,
                  task_status: task_status,
                  updated: true
                });
              }
            })
          }
        });
      }
      else {
        pool.query('UPDATE automata_task_results SET task_status=$1 WHERE task_id = $2 AND user_id = $3;', [task_status, task_id, user_id], (error, result2) => {
          if (error) { throw error }
          res.status(HTTP_OK).send(
            {
              task_id: task_id,
              user_id: user_id,
              task_status: task_status,
              updated: true
            }
          );
        });
      }
    }
    else {
      filesystem.mkdir("./uploads/" + user_id + "/", { recursive: true }, (err) => {
        if (err) { throw err };
      });
      filesystem.copyFile("./uploads/automataTasks/" + task_id + ".cmst", "./uploads/" + user_id + "/" + task_id + ".cmst", (error) => {
        if (error) { throw error; }
      }
      );
      pool.query("INSERT INTO automata_task_results(task_id, time_elapsed, task_status, user_id) VALUES ($1, $2, $3, $4);", [task_id, '00:00:00', task_status, user_id], (error, result2) => {
        if (error) { throw error }
        res.status(HTTP_OK).send(
          {
            task_id: task_id,
            user_id: user_id,
            task_status: task_status,
            updated: true
          }
        )
      })
    }
  })
})

app.get('/api/tasks/getFlag', (req, res) => {
  const task_id = req.query.task_id;
  const user_id = req.query.user_id;

  pool.query('SELECT task_status FROM automata_task_results WHERE task_id = $1 AND user_id = $2;', [task_id, user_id], (error, results) => {
    if (error) { throw error }
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(
        {
          task_id: task_id,
          user_id: user_id,
          task_status: results.rows[0].task_status
        }
      )
    }
    else {
      res.status(HTTP_OK).send(
        {
          task_id: task_id,
          user_id: user_id,
          task_status: 'new'
        }
      )
    }
  })
})

app.get('/api/tasks/download', (req, res) => {
  const task_id = req.query.task_id;
  const user_id = req.query.user_id;

  if (!user_id) {
    pool.query('SELECT * FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) => {
      if (err) { throw err }
      {
        if (result.rowCount > 0) {
          const filePath = "./uploads/automataTasks/" + task_id + ".cmst";
          res.status(HTTP_OK).download(filePath);
        }
        else {
          res.status(HTTP_NOT_FOUND).send(
            {
              task_id: task_id,
              found: false
            }
          );
        }
      }
    })
  }
  else {
    pool.query('SELECT * FROM automata_task_results WHERE user_id = $1 AND task_id = $2;', [user_id, task_id], (error, results) => {
      if (error) { throw error }
      if (results.rowCount > 0) {
        pool.query('SELECT task_id FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) => {
          if (err) { throw err }
          {
            if (result.rowCount > 0) {
              const filePath = "./uploads/" + user_id + "/" + task_id + ".cmst";
              res.status(HTTP_OK).download(filePath);
            }
            else {
              res.status(HTTP_NOT_FOUND).send(
                {
                  task_id: task_id,
                  found: false
                }
              );
            }
          }
        })
      }
      else {
        pool.query('SELECT task_id FROM automata_tasks WHERE task_id = $1;', [task_id], (err, result) => {
          if (err) { throw err }
          {
            if (result.rowCount > 0) {
              const filePath = "./uploads/automataTasks/" + task_id + ".cmst";
              res.status(HTTP_OK).download(filePath);
            }
            else {
              res.status(HTTP_NOT_FOUND).send(
                {
                  task_id: task_id,
                  found: false
                }
              );
            }
          }
        })
      }
    })
  }
})


app.post('/api/grammarTasks/upload', (req, res, next) => {
  const file = req.files.task;
  const file_name = req.query.file_name;

  filesystem.mkdir("./uploads/", { recursive: true }, (err) => {
    if (err) { throw err };
  });
  filesystem.mkdir("./uploads/grammarTasks/", { recursive: true }, (err) => {
    if (err) { throw err };
  });
  if (!file) {
    console.log(req);
    console.log("FILE NOT SAVED!");
  }
  else {
    file.mv("./uploads/grammarTasks/" + file_name + ".cmsg", function (err, results) {
      if (err) throw err;
      res.send({
        success: true,
        message: "File uploaded!"
      });
      console.log("NEW TASK UPLOADED!");
    });
  }

});

app.get('/api/grammarTasks/add', (req, res) => {
  const task_name = req.query.task_name;
  const task_description = req.query.task_description;
  const time = req.query.time;
  const assigner = req.query.assigner;
  const public_input = req.query.public_input;


  pool.query('INSERT INTO grammar_tasks(assigner_id, public_input, task_description, task_name, time) VALUES ($1, $2, $3, $4, $5);',
    [assigner, public_input, task_description, task_name, time], (err, results) => {
      if (err) { throw err; }

      if (results.rowCount > 0) {
        pool.query('SELECT * FROM grammar_tasks WHERE assigner_id = $1 AND task_name = $2 AND task_description = $3;', [assigner, task_name, task_description], (error, result) => {
          res.status(HTTP_OK).send(
            {
              task_id: result.rows[0].task_id,
              task_name: task_name,
              task_description: task_description,
              time: time,
              public_input: public_input
            }
          )
        })
      }
    });

})

app.get('/api/grammarTasks/getTasks', (req, res) => {
  const user_id = req.query.user_id;
  const auth_key = '\\x' + req.query.auth_key;

  pool.query('select gt.*, gtr.task_status, gt.time - gtr.time_elapsed as remaining_time, gtr.submitted, gtr.submission_date from grammar_tasks as gt left join (SELECT * from grammar_task_results where user_id=$1) as gtr on gtr.task_id = gt.task_id;', [user_id], (err, results) => {

    if (err) { throw err }
    if (results.rowCount > 0) {
      res.status(HTTP_OK).send(results.rows);
      console.log(Date(), [user_id], 'has fetched tasks!');
    }
    else {
      res.status(HTTP_OK).send({
        not_found: true
      });
    }
  })
})

app.get('/api/grammarTasks/delete', (req, res) => {
  const task_id = req.query.task_id;

  pool.query('SELECT * FROM grammar_tasks WHERE task_id = $1;', [task_id], (err, result) => {
    if (err) { throw err }
    if (result.rowCount > 0) {
      const filename = "./uploads/grammarTasks/" + result.rows[0].task_id + ".cmsg";
      filesystem.exists(filename, (exists) => {
        if (exists) {
          filesystem.unlink(filename, (error) => {
            if (error) {
              throw error;
            }
          });
        }
        else {
          console.log("TASK COULD NOT BE DELETED! FILE DOES NOT EXIST!");
        }
      });
    }
  })
  pool.query('DELETE FROM grammar_task_results WHERE task_id = $1;', [task_id], (err, result) => {
    if (err) { throw err }
    if (result.rowCount > 0) {
      pool.query('DELETE FROM grammar_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
        if (error) { throw error }
        console.log([task_id], "Task has been deleted!");
        res.status(HTTP_OK).send({
          task_id: task_id,
          deleted: true
        });
      })
    }
    else {
      pool.query('DELETE FROM grammar_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
        if (error) { throw error }
        console.log([task_id], "Task has been deleted!");
        res.status(HTTP_OK).send({
          task_id: task_id,
          deleted: true
        });
      })
    }
  })
})

app.get('/api/grammarTasks/changeFlag', (req, res) => {
  const task_id = req.query.task_id;
  const user_id = req.query.user_id;
  const task_status = req.query.task_status;

  pool.query('SELECT * FROM grammar_task_results WHERE task_id = $1 AND user_id = $2;', [task_id, user_id], (err, result) => {
    if (err) { throw err; }
    if (result.rowCount > 0) {
      if (task_status == "too_late") {
        pool.query('SELECT time FROM grammar_tasks where task_id = $1;', [task_id], (er, rs) => {
          if (er) { throw er }
          if (rs.rowCount > 0) {
            const available_time = rs.rows[0].time;
            var interval = "";
            if (available_time.hours) {
              interval += available_time.hours + ":";
            }
            else {
              interval += "00:";
            }

            if (available_time.minutes) {
              interval += available_time.minutes + ":";
            }
            else {
              interval += "00:";
            }

            if (available_time.seconds) {
              interval += available_time.seconds;
            }
            else {
              interval += "00";
            }

            pool.query('UPDATE grammar_task_results SET task_status=\'too_late\', time_elapsed = $1 WHERE task_id = $2 AND user_id = $3;', [interval, task_id, user_id], (e, r) => {
              if (e) { throw e }
              if (r.rowCount > 0) {
                res.status(HTTP_OK).send({
                  task_id: task_id,
                  user_id: user_id,
                  task_status: task_status,
                  updated: true
                });
              }
              else {
                res.status(HTTP_OK).send({
                  task_id: task_id,
                  user_id: user_id,
                  task_status: task_status,
                  updated: true
                });
              }
            })
          }
        });
      }
      else {
        pool.query('UPDATE grammar_task_results SET task_status=$1 WHERE task_id = $2 AND user_id = $3;', [task_status, task_id, user_id], (error, result2) => {
          if (error) { throw error }
          res.status(HTTP_OK).send(
            {
              task_id: task_id,
              user_id: user_id,
              task_status: task_status,
              updated: true
            }
          );
        });
      }
    }
    else {
      filesystem.mkdir("./uploads/" + user_id + "/", { recursive: true }, (err) => {
        if (err) { throw err };
      });
      filesystem.copyFile("./uploads/grammarTasks/" + task_id + ".cmsg", "./uploads/" + user_id + "/" + task_id + ".cmsg", (error) => {
        if (error) { throw error; }
      }
      );
      pool.query("INSERT INTO grammar_task_results(task_id, time_elapsed, task_status, user_id) VALUES ($1, $2, $3, $4);", [task_id, '00:00:00', task_status, user_id], (error, result2) => {
        if (error) { throw error }
        res.status(HTTP_OK).send(
          {
            task_id: task_id,
            user_id: user_id,
            task_status: task_status,
            updated: true
          }
        )
      })
    }
  })
})

app.listen(port, () => console.log(`CMServer server listening on port ${port}!`))